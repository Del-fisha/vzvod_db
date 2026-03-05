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
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Интеграционный тест Penalty")
public class PenaltyIntegrationTest {

    @Autowired
    SystemAuthenticator systemAuthenticator;

    @Autowired
    DataManager dataManager;

    Penalty penalty;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
        penalty = dataManager.create(Penalty.class);
        PreTestEntities.updatePenalty(penalty);
    }

    @Test
    @DisplayName("Тест сохранения в БД")
    void testSave() {
        Penalty savedPenalty = dataManager.save(penalty);
        UUID penaltyId = savedPenalty.getId();

        assertNotNull(penaltyId);

        Penalty loadedPenalty = dataManager.load(Penalty.class).id(penaltyId).one();

        assertEquals(savedPenalty.getPenaltyType(), loadedPenalty.getPenaltyType());
        assertEquals(savedPenalty.getPenaltyStatus(), loadedPenalty.getPenaltyStatus());
        assertEquals(savedPenalty.getInitiator(), loadedPenalty.getInitiator());
        assertEquals(savedPenalty.getDate(), loadedPenalty.getDate());
        assertEquals(savedPenalty.getOrderNumber(), loadedPenalty.getOrderNumber());
        assertEquals(savedPenalty.getDescription(), loadedPenalty.getDescription());
        assertEquals(penaltyId, loadedPenalty.getId());

        assertEquals(PenaltyType.REPRIMAND, loadedPenalty.getPenaltyType());
        assertEquals(PenaltyStatus.ACTIVE, loadedPenalty.getPenaltyStatus());
        assertEquals(Initiator.METRO, loadedPenalty.getInitiator());
        assertEquals(LocalDate.now(), loadedPenalty.getDate());
    }

    @Test
    @DisplayName("Тест изменения в БД")
    void updateTest() {
        Penalty savedPenalty = dataManager.save(penalty);
        UUID penaltyId = savedPenalty.getId();

        Penalty loadedPenalty = dataManager.load(Penalty.class).id(penaltyId).one();

        loadedPenalty.setPenaltyType(PenaltyType.DEMOTION);
        loadedPenalty.setPenaltyStatus(PenaltyStatus.REMOVED);
        loadedPenalty.setInitiator(Initiator.GU);
        loadedPenalty.setDate(LocalDate.now().minusDays(5));
        loadedPenalty.setOrderNumber("54321");
        loadedPenalty.setDescription("Updated description");

        Penalty updatedPenalty = dataManager.save(loadedPenalty);

        Penalty finalPenalty = dataManager.load(Penalty.class).id(penaltyId).one();

        assertEquals(updatedPenalty.getPenaltyType(), finalPenalty.getPenaltyType());
        assertEquals(updatedPenalty.getPenaltyStatus(), finalPenalty.getPenaltyStatus());
        assertEquals(updatedPenalty.getInitiator(), finalPenalty.getInitiator());
        assertEquals(updatedPenalty.getDate(), finalPenalty.getDate());
        assertEquals(updatedPenalty.getOrderNumber(), finalPenalty.getOrderNumber());
        assertEquals(updatedPenalty.getDescription(), finalPenalty.getDescription());

        assertEquals(PenaltyType.DEMOTION, finalPenalty.getPenaltyType());
        assertEquals(PenaltyStatus.REMOVED, finalPenalty.getPenaltyStatus());
        assertEquals(Initiator.GU, finalPenalty.getInitiator());
        assertEquals(LocalDate.now().minusDays(5), finalPenalty.getDate());
        assertEquals("54321", finalPenalty.getOrderNumber());
        assertEquals("Updated description", finalPenalty.getDescription());
    }

    @Test
    @DisplayName("Тест удаления из БД")
    void testDelete() {
        Penalty savedPenalty = dataManager.save(penalty);
        UUID penaltyId = savedPenalty.getId();

        dataManager.remove(savedPenalty);

        Penalty loadedPenalty = dataManager.load(Penalty.class)
                .id(penaltyId)
                .optional()
                .orElse(null);
        assertNull(loadedPenalty);
    }

    @Test
    @DisplayName("Тест каскадного удаления (один Penalty)")
    void cascadeDeleteTest() {
        User user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);
        user = dataManager.save(user);

        ServiceInfo serviceInfo = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(serviceInfo);
        serviceInfo.setUser(user);

        Penalty penalty = dataManager.create(Penalty.class);
        PreTestEntities.updatePenalty(penalty);
        penalty.setUserServiceInfo(serviceInfo);
        serviceInfo.getPenalty().add(penalty);

        ServiceInfo savedServiceInfo = dataManager.save(serviceInfo);

        assertEquals(1, savedServiceInfo.getPenalty().size());
        UUID penaltyId = savedServiceInfo.getPenalty().getFirst().getId();
        assertNotNull(penaltyId);

        dataManager.remove(savedServiceInfo);

        Penalty loaded = dataManager.load(Penalty.class)
                .id(penaltyId).optional().orElse(null);
        assertNull(loaded);
    }

    @Test
    @DisplayName("Тест каскадного сохранения и удаления с несколькими Penalty")
    void testCascadeWithMultiplePenalties() {
        User user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);
        user = dataManager.save(user);

        ServiceInfo serviceInfo = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(serviceInfo);
        serviceInfo.setUser(user);

        Penalty p1 = createPenalty("First penalty", PenaltyType.REPRIMAND);
        p1.setUserServiceInfo(serviceInfo);
        serviceInfo.getPenalty().add(p1);

        Penalty p2 = createPenalty("Second penalty", PenaltyType.REPRIMAND);
        p2.setUserServiceInfo(serviceInfo);
        serviceInfo.getPenalty().add(p2);

        Penalty p3 = createPenalty("Third penalty", PenaltyType.DEMOTION);
        p3.setUserServiceInfo(serviceInfo);
        serviceInfo.getPenalty().add(p3);

        ServiceInfo savedServiceInfo = dataManager.save(serviceInfo);

        assertNotNull(savedServiceInfo.getId());
        assertEquals(3, savedServiceInfo.getPenalty().size());

        UUID p1Id = savedServiceInfo.getPenalty().get(0).getId();
        UUID p2Id = savedServiceInfo.getPenalty().get(1).getId();
        UUID p3Id = savedServiceInfo.getPenalty().get(2).getId();

        Penalty toDelete = dataManager.load(Penalty.class).id(p1Id).one();
        dataManager.remove(toDelete);

        ServiceInfo serviceAfterDelete = dataManager.load(ServiceInfo.class)
                .id(savedServiceInfo.getId()).one();
        assertNotNull(serviceAfterDelete);

        assertTrue(dataManager.load(Penalty.class).id(p1Id).optional().isEmpty());
        assertTrue(dataManager.load(Penalty.class).id(p2Id).optional().isPresent());
        assertTrue(dataManager.load(Penalty.class).id(p3Id).optional().isPresent());

        dataManager.remove(serviceAfterDelete);

        assertTrue(dataManager.load(Penalty.class).id(p2Id).optional().isEmpty());
        assertTrue(dataManager.load(Penalty.class).id(p3Id).optional().isEmpty());
    }

    private Penalty createPenalty(String description, PenaltyType type) {
        Penalty p = dataManager.create(Penalty.class);
        PreTestEntities.updatePenalty(p);
        p.setDescription(description);
        p.setPenaltyType(type);
        return p;
    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.end();
    }
}