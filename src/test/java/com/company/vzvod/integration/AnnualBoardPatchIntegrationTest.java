package com.company.vzvod.integration;

import com.company.vzvod.annual.AnnualBoardService;
import com.company.vzvod.annual.AnnualDtos.AnnualItemDto;
import com.company.vzvod.annual.AnnualDtos.AnnualPatchRequest;
import com.company.vzvod.entity.AnnualMark;
import com.company.vzvod.entity.Department;
import com.company.vzvod.entity.IdCard;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.StatusInService;
import com.company.vzvod.entity.User;
import com.company.vzvod.test_support.PreTestEntities;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test-postgres")
class AnnualBoardPatchIntegrationTest {

    @Autowired
    private AnnualBoardService annualBoardService;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    private UUID userId;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
        userId = persistUser();
    }

    @AfterEach
    void tearDown() {
        if (userId != null) {
            systemAuthenticator.runWithSystem(this::removeUser);
        }
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("Галочка врача сохраняется и не затирает статус служебной карточки")
    void doctorCheckboxSurvivesPartialServiceInfo() {
        annualBoardService.patchOwn(userId, new AnnualPatchRequest("LOR", true, null, null, null));

        ServiceInfo reloaded = dataManager.load(ServiceInfo.class)
                .query("select s from ServiceInfo s where s.user.id = :id")
                .parameter("id", userId)
                .one();
        assertEquals(StatusInService.ACTIVE, reloaded.getStatus());
        assertFalse(Boolean.TRUE.equals(reloaded.getMedicalExamination()));

        AnnualItemDto lor = annualBoardService.loadOwn(userId).items().stream()
                .filter(item -> "LOR".equals(item.kind()))
                .findFirst()
                .orElseThrow();
        assertTrue(lor.checked());
        assertTrue(lor.counts());
    }

    private UUID persistUser() {
        User user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);
        user.setUsername("ann" + System.nanoTime());

        ServiceInfo serviceInfo = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(serviceInfo);
        serviceInfo.setToken(String.valueOf(System.nanoTime()).substring(0, 8));
        serviceInfo.setMedicalExamination(null);
        serviceInfo.setUser(user);
        user.setServiceInfo(serviceInfo);

        Department department = dataManager.create(Department.class);
        PreTestEntities.updateDepartment(department);
        department = dataManager.save(department);
        serviceInfo.setDepartment(department);

        IdCard idCard = dataManager.create(IdCard.class);
        PreTestEntities.updateIdCard(idCard);
        idCard.setSpl(String.valueOf(System.nanoTime()).substring(0, 6));
        idCard = dataManager.save(idCard);
        serviceInfo.setIdCard(idCard);

        return dataManager.save(user).getId();
    }

    private void removeUser() {
        dataManager.load(AnnualMark.class)
                .query("select m from AnnualMark m where m.serviceInfo.user.id = :id")
                .parameter("id", userId)
                .list()
                .forEach(dataManager::remove);
        dataManager.load(User.class).id(userId).optional().ifPresent(dataManager::remove);
    }
}
