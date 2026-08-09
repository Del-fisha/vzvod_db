package com.company.vzvod.integration;

import com.company.vzvod.entity.ArmyService;
import com.company.vzvod.entity.Department;
import com.company.vzvod.entity.IdCard;
import com.company.vzvod.entity.NumberOfShift;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.StatusInService;
import com.company.vzvod.entity.TypeOfShift;
import com.company.vzvod.entity.User;
import com.company.vzvod.entity.UserMobileBinding;
import com.company.vzvod.service.DepartmentConverter;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("GET /api/mobile/me/today-dashboard")
class MobileMeTodayDashboardIntegrationTest {

    private static final String TOKEN = "test-mobile-token-today-dashboard";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    private UUID createdUserId;
    private final List<UUID> createdShiftIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.runWithSystem(() -> {
            for (UUID shiftId : createdShiftIds) {
                dataManager.load(Shift.class).id(shiftId).optional().ifPresent(dataManager::remove);
            }
            createdShiftIds.clear();
            if (createdUserId != null) {
                dataManager.load(UserMobileBinding.class)
                        .query("select b from UserMobileBinding b where b.user.id = :uid")
                        .parameter("uid", createdUserId)
                        .list()
                        .forEach(dataManager::remove);
                dataManager.load(User.class).id(createdUserId).optional().ifPresent(dataManager::remove);
            }
        });
        createdUserId = null;
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("200: доступен любому аутентифицированному пользователю (не только с полным доступом)")
    void todayDashboard_ok_forAnyAuthenticatedUser() throws Exception {
        persistUserWithMobileBinding();
        LocalDate date = LocalDate.now();
        createShift(date, NumberOfShift._28, 3, 2);

        mockMvc.perform(get("/api/mobile/me/today-dashboard")
                        .header("X-Mobile-Token", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operationalDate").exists())
                .andExpect(jsonPath("$.routes").isArray())
                .andExpect(jsonPath("$.totals").exists());
    }

    @Test
    @DisplayName("401 без X-Mobile-Token")
    void todayDashboard_missingToken_returns401() throws Exception {
        mockMvc.perform(get("/api/mobile/me/today-dashboard"))
                .andExpect(status().isUnauthorized());
    }

    private void createShift(LocalDate date, NumberOfShift number, int ibdr, int migrant) {
        systemAuthenticator.runWithSystem(() -> {
            Shift shift = dataManager.create(Shift.class);
            shift.setDate(date);
            shift.setDepartmentToday(DepartmentConverter.departmentFromDate(date));
            shift.setNumber(number);
            shift.setTypeOfShift(TypeOfShift.VZVOD_ROUTE);
            shift.setStartTime(LocalTime.of(9, 0));
            shift.setIbdr(ibdr);
            shift.setMigrant(migrant);
            Shift saved = dataManager.save(shift);
            createdShiftIds.add(saved.getId());
        });
    }

    private void persistUserWithMobileBinding() {
        User user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);
        user.setArmyService(ArmyService.NOT_SERVED);

        ServiceInfo serviceInfo = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(serviceInfo);
        serviceInfo.setUser(user);
        user.setServiceInfo(serviceInfo);
        serviceInfo.setStatus(StatusInService.ACTIVE);

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

        UserMobileBinding binding = dataManager.create(UserMobileBinding.class);
        binding.setUser(user);
        binding.setToken(TOKEN);
        binding.setRegisteredAt(OffsetDateTime.now(ZoneOffset.UTC));
        dataManager.save(binding);
    }
}
