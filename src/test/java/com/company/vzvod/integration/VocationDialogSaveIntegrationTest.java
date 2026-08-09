package com.company.vzvod.integration;

import com.company.vzvod.entity.*;
import com.company.vzvod.service.VocationDialogSaveService;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Интеграционный тест сохранения Vocation из диалога")
class VocationDialogSaveIntegrationTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    @Autowired
    private VocationDialogSaveService vocationDialogSaveService;

    private ServiceInfo serviceInfo;
    private UUID createdUserId;
    private UUID createdDepartmentId;
    private UUID createdServiceInfoId;
    private UUID createdVocationId;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();

        User user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);
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
        if (createdVocationId != null) {
            dataManager.load(Vocation.class).id(createdVocationId).optional().ifPresent(dataManager::remove);
            createdVocationId = null;
        }
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
    @DisplayName("saveFromDialog создаёт отпуск и сохраняет все редактируемые поля")
    void saveFromDialog_createsWithAllEditableFields() {
        int year = LocalDate.now().getYear();

        Vocation edited = dataManager.create(Vocation.class);
        edited.setUserServiceInfo(serviceInfo);
        edited.setType(VocationType.ADDITIONAL);
        edited.setStartDate(LocalDate.of(year, 7, 1));
        edited.setEndDate(LocalDate.of(year, 7, 5));
        edited.setCityToDrive("Казань");
        edited.setDaysAddedByDeparture(1);
        edited.setHasDeparture(true);

        Vocation saved = vocationDialogSaveService.saveFromDialog(edited);
        assertNotNull(saved.getId());
        createdVocationId = saved.getId();

        Vocation loaded = dataManager.load(Vocation.class).id(saved.getId()).one();
        assertEquals(VocationType.ADDITIONAL, loaded.getType());
        assertEquals(LocalDate.of(year, 7, 1), loaded.getStartDate());
        assertEquals(LocalDate.of(year, 7, 5), loaded.getEndDate());
        assertEquals(5, loaded.getCountOfDays());
        assertEquals("Казань", loaded.getCityToDrive());
        assertEquals(1, loaded.getDaysAddedByDeparture());
        assertEquals(serviceInfo.getId(), loaded.getUserServiceInfo().getId());
    }

    @Test
    @DisplayName("saveFromDialog сохраняет смену вида отпуска и остальных изменённых полей")
    void saveFromDialog_updatesTypeAndOtherChangedFields() {
        int year = LocalDate.now().getYear();

        Vocation vocation = dataManager.create(Vocation.class);
        vocation.setUserServiceInfo(serviceInfo);
        vocation.setType(VocationType.MAIN);
        vocation.setStartDate(LocalDate.of(year, 8, 1));
        vocation.setEndDate(LocalDate.of(year, 8, 10));
        vocation.setCityToDrive("Москва");
        vocation.setDaysAddedByDeparture(2);
        vocation.setHasDeparture(true);
        vocation = vocationDialogSaveService.saveFromDialog(vocation);
        createdVocationId = vocation.getId();

        vocation.setType(VocationType.ADDITIONAL);
        vocation.setStartDate(LocalDate.of(year, 8, 2));
        vocation.setEndDate(LocalDate.of(year, 8, 8));
        vocation.setCityToDrive("Самара");
        vocation.setDaysAddedByDeparture(0);

        Vocation updated = vocationDialogSaveService.saveFromDialog(vocation);

        Vocation loaded = dataManager.load(Vocation.class).id(updated.getId()).one();
        assertEquals(VocationType.ADDITIONAL, loaded.getType());
        assertEquals(LocalDate.of(year, 8, 2), loaded.getStartDate());
        assertEquals(LocalDate.of(year, 8, 8), loaded.getEndDate());
        assertEquals(7, loaded.getCountOfDays());
        assertEquals("Самара", loaded.getCityToDrive());
        assertEquals(0, loaded.getDaysAddedByDeparture());
    }

    @Test
    @DisplayName("saveFromDialog без изменений оставляет прежние значения")
    void saveFromDialog_withoutChanges_keepsPreviousValues() {
        int year = LocalDate.now().getYear();

        Vocation vocation = dataManager.create(Vocation.class);
        vocation.setUserServiceInfo(serviceInfo);
        vocation.setType(VocationType.PART_OF_MAIN);
        vocation.setStartDate(LocalDate.of(year, 9, 1));
        vocation.setEndDate(LocalDate.of(year, 9, 3));
        vocation.setCityToDrive("Уфа");
        vocation.setDaysAddedByDeparture(0);
        vocation = vocationDialogSaveService.saveFromDialog(vocation);
        createdVocationId = vocation.getId();

        Vocation reloadedForEdit = dataManager.load(Vocation.class).id(vocation.getId()).one();
        Vocation savedAgain = vocationDialogSaveService.saveFromDialog(reloadedForEdit);

        Vocation loaded = dataManager.load(Vocation.class).id(savedAgain.getId()).one();
        assertEquals(VocationType.PART_OF_MAIN, loaded.getType());
        assertEquals(LocalDate.of(year, 9, 1), loaded.getStartDate());
        assertEquals(LocalDate.of(year, 9, 3), loaded.getEndDate());
        assertEquals("Уфа", loaded.getCityToDrive());
        assertEquals(0, loaded.getDaysAddedByDeparture());
    }
}
