package com.company.vzvod.integration;

import com.company.vzvod.bot.dto.BotShiftUpsertRequest;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    private ObjectMapper objectMapper;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    private UUID createdUserId;
    private UUID createdPartnerUserId;
    private UUID createdShiftId;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
    }

    @AfterEach
    void tearDown() {
        if (createdUserId != null || createdPartnerUserId != null) {
            systemAuthenticator.runWithSystem(() -> {
                if (createdUserId != null) {
                    dataManager.load(User.class).id(createdUserId).optional().ifPresent(user -> {
                        if (user.getServiceInfo() != null) {
                            UUID sid = user.getServiceInfo().getId();
                            dataManager.load(Shift.class)
                                    .query("select s from Shift s join s.units u where u.id = :sid")
                                    .parameter("sid", sid)
                                    .list()
                                    .forEach(dataManager::remove);
                        }
                    });
                    dataManager.load(UserTelegramBinding.class)
                            .query("select b from UserTelegramBinding b where b.user.id = :uid")
                            .parameter("uid", createdUserId)
                            .list()
                            .forEach(dataManager::remove);
                    dataManager.load(User.class).id(createdUserId).optional().ifPresent(dataManager::remove);
                }
                if (createdPartnerUserId != null) {
                    dataManager.load(User.class).id(createdPartnerUserId).optional().ifPresent(user -> {
                        if (user.getServiceInfo() != null) {
                            UUID sid = user.getServiceInfo().getId();
                            dataManager.load(Shift.class)
                                    .query("select s from Shift s join s.units u where u.id = :sid")
                                    .parameter("sid", sid)
                                    .list()
                                    .forEach(dataManager::remove);
                        }
                    });
                    dataManager.load(User.class).id(createdPartnerUserId).optional().ifPresent(dataManager::remove);
                }
            });
            createdUserId = null;
            createdPartnerUserId = null;
            createdShiftId = null;
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

    private UUID persistActiveColleagueInDepartment(Department department) {
        User colleague = dataManager.create(User.class);
        PreTestEntities.updateUser(colleague);
        colleague.setArmyService(ArmyService.NOT_SERVED);

        ServiceInfo colleagueSi = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(colleagueSi);
        colleagueSi.setUser(colleague);
        colleague.setServiceInfo(colleagueSi);
        colleagueSi.setStatus(StatusInService.ACTIVE);
        colleagueSi.setDepartment(department);

        IdCard idCard = dataManager.create(IdCard.class);
        PreTestEntities.updateIdCard(idCard);
        idCard = dataManager.save(idCard);
        colleagueSi.setIdCard(idCard);

        colleague = dataManager.save(colleague);
        createdPartnerUserId = colleague.getId();

        Contacts contacts = dataManager.create(Contacts.class);
        contacts.setUser(colleague);
        dataManager.save(contacts);

        return colleague.getServiceInfo().getId();
    }

    private void persistUserBindingOnly() {
        User user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);
        user.setArmyService(ArmyService.NOT_SERVED);

        ServiceInfo serviceInfo = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(serviceInfo);
        serviceInfo.setUser(user);
        user.setServiceInfo(serviceInfo);
        serviceInfo.setStatus(StatusInService.ACTIVE);
        serviceInfo.setVacationDaysEntitled(40);
        serviceInfo.setVacationDaysAvailable(40);

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

        UserTelegramBinding binding = dataManager.create(UserTelegramBinding.class);
        binding.setUser(user);
        binding.setChatId(CHAT_ID);
        binding.setRegisteredAt(OffsetDateTime.now(ZoneOffset.UTC));
        dataManager.save(binding);

        ServiceInfo siReload = dataManager.load(ServiceInfo.class).id(serviceInfo.getId()).one();
        siReload.setVacationDaysEntitled(40);
        siReload.setVacationDaysAvailable(40);
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
                .andExpect(jsonPath("$.items[0].id").exists())
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

    @Test
    @DisplayName("POST /shifts — создание смены (без времени окончания, с напарником)")
    void postShift_created() throws Exception {
        persistUserBindingOnly();
        final UUID[] partnerSid = new UUID[1];
        systemAuthenticator.runWithSystem(() -> {
            User bound = dataManager.load(User.class).id(createdUserId).one();
            Department dep = bound.getServiceInfo().getDepartment();
            partnerSid[0] = persistActiveColleagueInDepartment(dep);
        });

        BotShiftUpsertRequest req = new BotShiftUpsertRequest(
                LocalDate.of(2026, 5, 10),
                "МП 31",
                "VZVOD_ROUTE",
                LocalTime.of(8, 0),
                null,
                partnerSid[0],
                null,
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/bot/me/shifts")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.route").value("МП 31"))
                .andExpect(jsonPath("$.shiftType").value("Маршрут взвода"))
                .andExpect(jsonPath("$.endTime").doesNotExist());
    }

    @Test
    @DisplayName("GET /colleagues — коллеги отделения без текущего пользователя")
    void colleagues_ok() throws Exception {
        persistUserBindingOnly();
        systemAuthenticator.runWithSystem(() -> {
            User bound = dataManager.load(User.class).id(createdUserId).one();
            Department dep = bound.getServiceInfo().getDepartment();
            persistActiveColleagueInDepartment(dep);
        });

        mockMvc.perform(get("/api/bot/me/colleagues")
                        .param("department", "1")
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].serviceInfoId").exists())
                .andExpect(jsonPath("$.items[0].label").value("Пётр П."));
    }

    @Test
    @DisplayName("PUT /shifts/{id} — обновление своей смены")
    void putShift_updates() throws Exception {
        persistUserShiftAndVocation();

        BotShiftUpsertRequest req = new BotShiftUpsertRequest(
                LocalDate.of(2026, 6, 1),
                "МП 32",
                TypeOfShift.BAT_POST.getId(),
                LocalTime.of(10, 0),
                LocalTime.of(22, 0),
                null,
                1,
                2,
                0,
                0
        );

        mockMvc.perform(put("/api/bot/me/shifts/" + createdShiftId)
                        .header("X-Api-Key", API_KEY)
                        .header("X-Telegram-Chat-Id", Long.toString(CHAT_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdShiftId.toString()))
                .andExpect(jsonPath("$.route").value("МП 32"))
                .andExpect(jsonPath("$.shiftType").value("Пост батальона"));
    }
}
