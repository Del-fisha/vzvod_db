package com.company.vzvod.integration;

import com.company.vzvod.entity.*;
import com.company.vzvod.service.VocationBalanceService;
import com.company.vzvod.service.VocationService;
import com.company.vzvod.test_support.PreTestEntities;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Интеграционный тест баланса отпусков")
public class VocationBalanceIntegrationTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    @Autowired
    private VocationBalanceService vocationBalanceService;

    private ServiceInfo serviceInfo;
    private UUID createdUserId;
    private UUID createdDepartmentId;
    private UUID createdServiceInfoId;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();

        User user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);
        user.setArmyService(ArmyService.NOT_SERVED);
        user = dataManager.save(user);
        createdUserId = user.getId();

        Department department = dataManager.create(Department.class);
        PreTestEntities.updateDepartment(department);
        department = dataManager.save(department);
        createdDepartmentId = department.getId();

        IdCard idCard = dataManager.create(IdCard.class);
        PreTestEntities.updateIdCard(idCard);
        idCard = dataManager.save(idCard);

        serviceInfo = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(serviceInfo);
        serviceInfo.setUser(user);
        serviceInfo.setDepartment(department);
        serviceInfo.setIdCard(idCard);
        serviceInfo.setStartDate(LocalDate.of(LocalDate.now().getYear() - 5, 1, 1));
        serviceInfo = dataManager.save(serviceInfo);
        createdServiceInfoId = serviceInfo.getId();
    }

    @AfterEach
    void tearDown() {
        if (createdServiceInfoId != null) {
            dataManager.load(ServiceInfo.class).id(createdServiceInfoId).optional().ifPresent(dataManager::remove);
            createdServiceInfoId = null;
        }
        if (createdDepartmentId != null) {
            dataManager.load(Department.class).id(createdDepartmentId).optional().ifPresent(dataManager::remove);
            createdDepartmentId = null;
        }
        if (createdUserId != null) {
            dataManager.load(User.class).id(createdUserId).optional().ifPresent(dataManager::remove);
            createdUserId = null;
        }
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("Доступно = (положено + добавлено) - использовано")
    void available_isEntitledPlusAddedMinusUsed() {
        int year = LocalDate.now().getYear();

        Vocation v1 = dataManager.create(Vocation.class);
        v1.setUserServiceInfo(serviceInfo);
        v1.setTypeId(VocationType.MAIN.getId());
        v1.setStartDate(LocalDate.of(year, 3, 1));
        v1.setEndDate(LocalDate.of(year, 3, 10)); // 10 дней
        v1.setHasDeparture(true);
        v1.setDaysAddedByDeparture(2);
        dataManager.save(v1);

        Vocation v2 = dataManager.create(Vocation.class);
        v2.setUserServiceInfo(serviceInfo);
        v2.setTypeId(VocationType.PART_OF_MAIN.getId());
        v2.setStartDate(LocalDate.of(year, 6, 1));
        v2.setEndDate(LocalDate.of(year, 6, 5)); // 5 дней
        v2.setHasDeparture(false);
        v2.setDaysAddedByDeparture(0);
        dataManager.save(v2);

        var stats = vocationBalanceService.recalcAndSave(serviceInfo.getId());

        int baseEntitled = VocationService.daysAvailable(serviceInfo, LocalDate.of(year, 1, 1));
        assertEquals(baseEntitled + 2, stats.entitled());
        assertEquals(15, stats.used());
        assertEquals((baseEntitled + 2) - 15, stats.available());
    }
}

