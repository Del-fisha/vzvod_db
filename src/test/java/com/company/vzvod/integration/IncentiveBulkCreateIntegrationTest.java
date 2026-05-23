package com.company.vzvod.integration;

import com.company.vzvod.entity.*;
import com.company.vzvod.service.IncentiveBulkCreateService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Интеграционный тест массового создания Incentive")
class IncentiveBulkCreateIntegrationTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    @Autowired
    private IncentiveBulkCreateService incentiveBulkCreateService;

    private final List<UUID> createdUserIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
    }

    @AfterEach
    void tearDown() {
        for (UUID id : createdUserIds) {
            if (id != null) {
                dataManager.load(User.class).id(id).optional().ifPresent(dataManager::remove);
            }
        }
        createdUserIds.clear();
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("createForServiceInfos создаёт отдельное поощрение для каждого сотрудника с одинаковыми полями")
    void createForServiceInfos_createsIncentivePerEmployeeWithSameFields() {
        ServiceInfo si1 = createPersistedServiceInfo();
        ServiceInfo si2 = createPersistedServiceInfo();

        Incentive template = dataManager.create(Incentive.class);
        PreTestEntities.updateIncentive(template);
        template.setDescription("Bulk reward");
        template.setOrderNumber("ORD-777");
        template.setIncentiveType(IncentiveType.DIPLOMA);
        template.setInitiator(Initiator.GU);
        template.setDate(LocalDate.of(2025, 3, 15));

        List<Incentive> created = incentiveBulkCreateService.createForServiceInfos(
                template, List.of(si1, si2));

        assertEquals(2, created.size());

        for (int i = 0; i < created.size(); i++) {
            Incentive saved = created.get(i);
            assertNotNull(saved.getId());
            assertEquals(IncentiveType.DIPLOMA, saved.getIncentiveType());
            assertEquals(Initiator.GU, saved.getInitiator());
            assertEquals(LocalDate.of(2025, 3, 15), saved.getDate());
            assertEquals("ORD-777", saved.getOrderNumber());
            assertEquals("Bulk reward", saved.getDescription());
        }

        UUID si1Id = si1.getId();
        UUID si2Id = si2.getId();
        assertEquals(si1Id, created.get(0).getUserServiceInfo().getId());
        assertEquals(si2Id, created.get(1).getUserServiceInfo().getId());
        assertNotEquals(created.get(0).getId(), created.get(1).getId());

        Incentive loaded1 = dataManager.load(Incentive.class).id(created.get(0).getId()).one();
        Incentive loaded2 = dataManager.load(Incentive.class).id(created.get(1).getId()).one();
        assertEquals(si1Id, loaded1.getUserServiceInfo().getId());
        assertEquals(si2Id, loaded2.getUserServiceInfo().getId());
    }

    @Test
    @DisplayName("createForServiceInfos для одного сотрудника создаёт одну запись")
    void createForServiceInfos_singleEmployee_createsOneIncentive() {
        ServiceInfo serviceInfo = createPersistedServiceInfo();

        Incentive template = dataManager.create(Incentive.class);
        PreTestEntities.updateIncentive(template);

        List<Incentive> created = incentiveBulkCreateService.createForServiceInfos(
                template, List.of(serviceInfo));

        assertEquals(1, created.size());
        assertEquals(serviceInfo.getId(), created.getFirst().getUserServiceInfo().getId());
        assertEquals(IncentiveType.BONUS, created.getFirst().getIncentiveType());
        assertEquals(Initiator.METRO, created.getFirst().getInitiator());
    }

    @Test
    @DisplayName("createForServiceInfos без сотрудников выбрасывает исключение")
    void createForServiceInfos_emptyEmployees_throws() {
        Incentive template = dataManager.create(Incentive.class);
        PreTestEntities.updateIncentive(template);

        assertThrows(IllegalArgumentException.class,
                () -> incentiveBulkCreateService.createForServiceInfos(template, List.of()));
    }

    @Test
    @DisplayName("createForServiceInfos без шаблона выбрасывает исключение")
    void createForServiceInfos_nullTemplate_throws() {
        ServiceInfo serviceInfo = createPersistedServiceInfo();

        assertThrows(IllegalArgumentException.class,
                () -> incentiveBulkCreateService.createForServiceInfos(null, List.of(serviceInfo)));
    }

    private ServiceInfo createPersistedServiceInfo() {
        User user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);
        user = dataManager.save(user);
        createdUserIds.add(user.getId());

        ServiceInfo serviceInfo = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(serviceInfo);
        serviceInfo.setUser(user);
        return dataManager.save(serviceInfo);
    }
}
