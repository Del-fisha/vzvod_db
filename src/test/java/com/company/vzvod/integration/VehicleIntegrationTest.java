package com.company.vzvod.integration;

import com.company.vzvod.entity.User;
import com.company.vzvod.entity.Vehicle;
import com.company.vzvod.test_support.PreTestEntities;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlans;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Интеграционный тест Vehicle")
public class VehicleIntegrationTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private FetchPlans fetchPlans;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    private Vehicle vehicle;
    private User user;
    private UUID createdUserId;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();

        user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);
        user = dataManager.save(user);
        createdUserId = user.getId();

        vehicle = dataManager.create(Vehicle.class);
        PreTestEntities.updateVehicle(vehicle);
        vehicle.setUser(user);
    }

    @AfterEach
    void tearDown() {
        // Clean up only entities created by this test
        if (createdUserId != null) {
            dataManager.load(User.class).id(createdUserId).optional().ifPresent(dataManager::remove);
            createdUserId = null;
        }
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("Тест сохранения в БД")
    void testSave() {
        Vehicle saved = dataManager.save(vehicle);
        UUID id = saved.getId();
        assertNotNull(id);

        Vehicle loaded = dataManager.load(Vehicle.class).id(id).one();
        assertEquals(saved.getStateNumber(), loaded.getStateNumber());
        assertEquals(saved.getModel(), loaded.getModel());
        assertEquals(saved.getBrand(), loaded.getBrand());
        assertEquals(saved.getRegistrationCertificate(), loaded.getRegistrationCertificate());
        assertEquals(saved.getInsurance(), loaded.getInsurance());
        assertEquals(saved.getUser().getId(), loaded.getUser().getId());

        FetchPlan userPlan = fetchPlans.builder(User.class)
                .add("vehicleInfo")
                .build();
        User loadedUser = dataManager.load(User.class)
                .id(saved.getUser().getId())
                .fetchPlan(userPlan)
                .one();
        assertNotNull(loadedUser.getVehicleInfo());
        assertTrue(loadedUser.getVehicleInfo().stream().anyMatch(v -> v.getId().equals(saved.getId())));
    }

    @Test
    @DisplayName("Тест изменения в БД")
    void testUpdate() {
        Vehicle saved = dataManager.save(vehicle);
        UUID id = saved.getId();

        Vehicle loaded = dataManager.load(Vehicle.class).id(id).one();
        loaded.setStateNumber("В456ММ198");
        loaded.setModel("Granta");
        loaded.setBrand("Lada");
        loaded.setRegistrationCertificate("98УТ456789");
        loaded.setInsurance(LocalDate.now().plusYears(1));

        Vehicle updated = dataManager.save(loaded);
        assertEquals("В456ММ198", updated.getStateNumber());
        assertEquals("Granta", updated.getModel());
    }

    @Test
    @DisplayName("Тест удаления из БД")
    void testDelete() {
        Vehicle saved = dataManager.save(vehicle);
        UUID id = saved.getId();

        dataManager.remove(saved);
        Vehicle deleted = dataManager.load(Vehicle.class).id(id).optional().orElse(null);
        assertNull(deleted);
    }
}