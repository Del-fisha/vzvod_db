package com.company.vzvod.integration;

import com.company.vzvod.entity.*;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Интеграционный тест Incentive")
public class IncentiveIntegrationTest {

    @Autowired
    SystemAuthenticator systemAuthenticator;

    @Autowired
    DataManager dataManager;

    Incentive incentive;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();

        incentive = dataManager.create(Incentive.class);
        PreTestEntities.updateIncentive(incentive);
    }

    @Test
    @DisplayName("Тест сохранения в БД")
    void testSave() {
        Incentive savedIncentive = dataManager.save(incentive);
        UUID incentiveId = savedIncentive.getId();

        assertNotNull(incentiveId);

        Incentive loadedIncentive = dataManager.load(Incentive.class).id(incentiveId).one();

        assertEquals(savedIncentive.getIncentiveType(), loadedIncentive.getIncentiveType());
        assertEquals(savedIncentive.getDescription(), loadedIncentive.getDescription());
        assertEquals(savedIncentive.getOrderNumber(), loadedIncentive.getOrderNumber());
        assertEquals(savedIncentive.getInitiator(), loadedIncentive.getInitiator());
        assertEquals(savedIncentive.getDate(), loadedIncentive.getDate());
        assertEquals(incentiveId, loadedIncentive.getId());

        assertEquals(IncentiveType.BONUS, loadedIncentive.getIncentiveType());
        assertEquals(Initiator.METRO, loadedIncentive.getInitiator());
        assertEquals(LocalDate.now(), loadedIncentive.getDate());
    }

    @Test
    @DisplayName("Тест изменения в БД")
    void updateTest() {
        Incentive savedIncentive = dataManager.save(incentive);
        UUID incentiveId = savedIncentive.getId();

        Incentive loadedIncentive = dataManager.load(Incentive.class).id(incentiveId).one();

        loadedIncentive.setIncentiveType(IncentiveType.MEDAL);
        loadedIncentive.setOrderNumber("000111");
        loadedIncentive.setInitiator(Initiator.GU);
        loadedIncentive.setDate(LocalDate.now().minusDays(12));
        loadedIncentive.setDescription("Something");

        Incentive incentiveToSave = dataManager.save(loadedIncentive);

        Incentive updatedIncentive = dataManager.load(Incentive.class).id(incentiveId).one();

        assertEquals(incentiveToSave.getIncentiveType(), updatedIncentive.getIncentiveType());
        assertEquals(incentiveToSave.getDescription(), updatedIncentive.getDescription());
        assertEquals(incentiveToSave.getOrderNumber(), updatedIncentive.getOrderNumber());
        assertEquals(incentiveToSave.getInitiator(), updatedIncentive.getInitiator());
        assertEquals(incentiveToSave.getDate(), updatedIncentive.getDate());
        assertEquals(incentiveId, updatedIncentive.getId());


        assertEquals(LocalDate.now().minusDays(12), updatedIncentive.getDate());
        assertEquals(IncentiveType.MEDAL, updatedIncentive.getIncentiveType());
        assertEquals("Something", loadedIncentive.getDescription());
        assertEquals("000111", updatedIncentive.getOrderNumber());
        assertEquals(Initiator.GU, updatedIncentive.getInitiator());
    }

    @Test
    @DisplayName("Тест удаления из БД")
    void testDelete() {
        Incentive savedIncentive = dataManager.save(incentive);
        UUID incentiveId = savedIncentive.getId();

        dataManager.remove(incentive);

        Incentive loadedIncentive = dataManager.load(Incentive.class)
                .id(incentiveId)
                .optional()
                .orElse(null);
        assertNull(loadedIncentive);
    }

    @Test
    @DisplayName("Тест каскадного удаления")
    void cascadeDeleteTest() {
        User user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);
        user = dataManager.save(user);

        ServiceInfo serviceInfo = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(serviceInfo);
        serviceInfo.setUser(user);

        Incentive incentive = dataManager.create(Incentive.class);
        PreTestEntities.updateIncentive(incentive);
        incentive.setUserServiceInfo(serviceInfo);
        serviceInfo.getIncentive().add(incentive);

        ServiceInfo savedServiceInfo = dataManager.save(serviceInfo);

        assertEquals(1, savedServiceInfo.getIncentive().size());
        UUID incentiveId = savedServiceInfo.getIncentive().getFirst().getId();
        assertNotNull(incentiveId);

        dataManager.remove(savedServiceInfo);

        Incentive loaded = dataManager.load(Incentive.class)
                .id(incentiveId).optional().orElse(null);
        assertNull(loaded);
    }

    @Test
    @DisplayName("Тест каскадного сохранения и удаления с несколькими Incentive")
    @Transactional
    void testCascadeWithMultipleIncentives() {
        User user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);
        user = dataManager.save(user);

        ServiceInfo serviceInfo = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(serviceInfo);
        serviceInfo.setUser(user);

        Incentive inc1 = createIncentive("First incentive", IncentiveType.BONUS);
        inc1.setUserServiceInfo(serviceInfo);
        serviceInfo.getIncentive().add(inc1);

        Incentive inc2 = createIncentive("Second incentive", IncentiveType.GRATITUDE);
        inc2.setUserServiceInfo(serviceInfo);
        serviceInfo.getIncentive().add(inc2);

        Incentive inc3 = createIncentive("Third incentive", IncentiveType.MEDAL);
        inc3.setUserServiceInfo(serviceInfo);
        serviceInfo.getIncentive().add(inc3);

        ServiceInfo savedServiceInfo = dataManager.save(serviceInfo);

        assertNotNull(savedServiceInfo.getId());
        assertEquals(3, savedServiceInfo.getIncentive().size());

        UUID inc1Id = savedServiceInfo.getIncentive().get(0).getId();
        UUID inc2Id = savedServiceInfo.getIncentive().get(1).getId();
        UUID inc3Id = savedServiceInfo.getIncentive().get(2).getId();

        Incentive toDelete = dataManager.load(Incentive.class).id(inc1Id).one();
        dataManager.remove(toDelete);

        ServiceInfo serviceAfterDelete = dataManager.load(ServiceInfo.class)
                .id(savedServiceInfo.getId()).one();
        assertNotNull(serviceAfterDelete);

        assertTrue(dataManager.load(Incentive.class).id(inc1Id).optional().isEmpty());
        assertTrue(dataManager.load(Incentive.class).id(inc2Id).optional().isPresent());
        assertTrue(dataManager.load(Incentive.class).id(inc3Id).optional().isPresent());

        dataManager.remove(serviceAfterDelete);

        assertTrue(dataManager.load(Incentive.class).id(inc2Id).optional().isEmpty());
        assertTrue(dataManager.load(Incentive.class).id(inc3Id).optional().isEmpty());
    }

    private Incentive createIncentive(String description, IncentiveType type) {
        Incentive inc = dataManager.create(Incentive.class);
        PreTestEntities.updateIncentive(inc);
        inc.setDescription(description);
        inc.setIncentiveType(type);
        return inc;
    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.end();
    }
}
