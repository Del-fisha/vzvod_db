package com.company.vzvod.integration;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import com.company.vzvod.test_support.PreTestEntities;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Интеграционный тест User (полный CRUD)")
public class UserIntegrationTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    private User user;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
        user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);
        user.setPassword(passwordEncoder.encode("password"));
    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("Тест сохранения в БД")
    void testSave() {
        User saved = dataManager.save(user);
        UUID id = saved.getId();
        assertNotNull(id);

        User loaded = dataManager.load(User.class).id(id).one();
        assertEquals(saved.getUsername(), loaded.getUsername());
        assertEquals(saved.getFirstName(), loaded.getFirstName());
        assertEquals(saved.getLastName(), loaded.getLastName());
        assertEquals(saved.getPatronymic(), loaded.getPatronymic());
        assertEquals(saved.getDateOfBirth(), loaded.getDateOfBirth());
    }

    @Test
    @DisplayName("Тест изменения в БД")
    void testUpdate() {
        User saved = dataManager.save(user);
        UUID id = saved.getId();

        User loaded = dataManager.load(User.class).id(id).one();
        loaded.setFirstName("Иван");
        loaded.setLastName("Иванов");
        loaded.setPatronymic("Иванович");
        loaded.setDateOfBirth(LocalDate.of(1985, 5, 10));

        User updated = dataManager.save(loaded);
        assertEquals("Иван", updated.getFirstName());
        assertEquals("Иванов", updated.getLastName());
        assertEquals("Иванович", updated.getPatronymic());
        assertEquals(LocalDate.of(1985, 5, 10), updated.getDateOfBirth());
    }

    @Test
    @DisplayName("Тест удаления из БД")
    void testDelete() {
        User saved = dataManager.save(user);
        UUID id = saved.getId();

        dataManager.remove(saved);
        User deleted = dataManager.load(User.class).id(id).optional().orElse(null);
        assertNull(deleted);
    }

    @Test
    @DisplayName("Тест каскадного сохранения и удаления ServiceInfo при операциях с User")
    void testCascadeSaveAndDeleteServiceInfo() {

        ServiceInfo serviceInfo = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(serviceInfo);
        serviceInfo.setUser(user);
        user.setServiceInfo(serviceInfo);

        User savedUser = dataManager.save(user);
        UUID userId = savedUser.getId();
        UUID serviceInfoId = savedUser.getServiceInfo().getId();
        assertNotNull(serviceInfoId);

        ServiceInfo loadedServiceInfo = dataManager.load(ServiceInfo.class).id(serviceInfoId).one();
        assertNotNull(loadedServiceInfo);
        assertEquals(userId, loadedServiceInfo.getUser().getId());

        entityManager.clear();

        dataManager.remove(savedUser);

        ServiceInfo deletedServiceInfo = dataManager.load(ServiceInfo.class)
                .id(serviceInfoId)
                .optional()
                .orElse(null);
        assertNull(deletedServiceInfo);
    }
}