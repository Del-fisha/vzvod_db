package com.company.vzvod.integration;

import com.company.vzvod.entity.ArmyService;
import com.company.vzvod.entity.Contacts;
import com.company.vzvod.entity.Dep;
import com.company.vzvod.entity.Department;
import com.company.vzvod.entity.IdCard;
import com.company.vzvod.entity.NumberOfShift;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.entity.StatusInService;
import com.company.vzvod.entity.TypeOfShift;
import com.company.vzvod.entity.User;
import com.company.vzvod.entity.UserTelegramBinding;
import com.company.vzvod.entity.Vocation;
import com.company.vzvod.entity.VocationType;
import com.company.vzvod.security.crypto.UserPiiEncryptionMigrator;
import com.company.vzvod.test_support.PreTestEntities;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("GET /api/bot/me/shifts и /vacations")
class BotMeShiftsVacationsIntegrationTest {

    private static final String API_KEY = "test-bot-api-key";
    private static final long CHAT_ID = 88_088_088_088L;

    @MockBean
    @SuppressWarnings("unused")
    private UserPiiEncryptionMigrator userPiiEncryptionMigrator;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    private UUID createdUserId;
    private UUID createdShiftId;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
    }

    @AfterEach
    void tearDown() {
        if (createdUserId != null) {
            systemAuthenticator.runWithSystem(() -> {
                if (createdShiftId != null) {
                    dataManager.load(Shift.class).id(createdShiftId).optional().ifPresent(dataManager::remove);
                    createdShiftId = null;
                }
                dataManager.load(UserTelegramBinding.class)
                        .query("select b from UserTelegramBinding b where b.user.id = :uid")
                        .parameter("uid", createdUserId)
                        .list()
                        .forEach(dataManager::remove);
                dataManager.load(User.class).id(createdUserId).optional().ifPresent(dataManager::remove);
            });
            createdUserId = null;
        }
        systemAuthenticator.end();
    }

    private void persistUserShiftAndVocation() {
        User user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);
        user.setArmyService(ArmyService.NOT_SERVED);

        ServiceInfo serviceInfo = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(serviceInfo);
        serviceInfo.setUser(user);
        user.setServiceInfo(serviceInfo);
        serviceInfo.setStatus(StatusInService.ACTIVE);
        serviceInfo.setVacationDaysEntitled(40);
        serviceInfo.setVacationDaysAvailable(28);

        Department department = dataManager.create(Department.class);
        PreTestEntities.updateDepartment(department);
        department = dataManager.save(department);
        serviceInfo.setDepartment(department);

        IdCard idCard = dataManager.create(IdCard.class);
        PreTestEntities.updateIdCard(idCard);
        idCard = dataManager.save(idCard);
        serviceInfo.setIdCard(idCard);

        user = dataManager.save(user);
        createdUserId = user.getId();

        Contacts contacts = dataManager.create(Contacts.class);
        contacts.setUser(user);
        dataManager.save(contacts);

        Shift shift = dataManager.create(Shift.class);
        shift.setDate(LocalDate.of(2026, 1, 15));
        shift.setNumber(NumberOfShift._28);
        shift.setTypeOfShift(TypeOfShift.VZVOD_ROUTE);
        shift.setDepartmentToday(Dep.FIRST);
        shift.setStartTime(LocalTime.of(8, 0));
        shift.setEndTime(LocalTime.of(20, 0));
        Set<ServiceInfo> units = new HashSet<>();
        units.add(serviceInfo);
        shift.setUnits(units);
        shift = dataManager.save(shift);
        createdShiftId = shift.getId();

        Vocation vocation = dataManager.create(Vocation.class);
        vocation.setUserServiceInfo(serviceInfo);
        vocation.setType(VocationType.MAIN);
        vocation.setStartDate(LocalDate.of(2025, 7, 1));
        vocation.setEndDate(LocalDate.of(2025, 7, 14));
        vocation.setCountOfDays(14);
        dataManager.save(vocation);

        UserTelegramBinding binding = dataManager.create(UserTelegramBinding.class);
        binding.setUser(user);
        binding.setChatId(CHAT_ID);
        binding.setRegisteredAt(OffsetDateTime.now(ZoneOffset.UTC));
        dataManager.save(binding);

        ServiceInfo siReload = dataManager.load(ServiceInfo.class).id(serviceInfo.getId()).one();
        siReload.setVacationDaysEntitled(40);
        siReload.setVacationDaysAvailable(28);
        dataManager.save(siReload);
    }

    @Test
    @DisplayName("GET /shifts — смены пользователя")
    void shifts_ok() throws Exception {
        persistUserShiftAndVocation();

        mockMvc.perform(get("/api/bot/me/shifts")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].route").value("МП 28"))
                .andExpect(jsonPath("$.items[0].shiftType").value("Маршрут взвода"));
    }

    @Test
    @DisplayName("GET /vacations — баланс и записи отпусков")
    void vacations_ok() throws Exception {
        persistUserShiftAndVocation();

        mockMvc.perform(get("/api/bot/me/vacations")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance.entitled").value(40))
                .andExpect(jsonPath("$.balance.available").value(28))
                .andExpect(jsonPath("$.balance.used").value(12))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].typeLabel").value("Основной"));
    }
}
