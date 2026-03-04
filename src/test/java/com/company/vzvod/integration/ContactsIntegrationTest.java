package com.company.vzvod.integration;

import com.company.vzvod.entity.Address;
import com.company.vzvod.entity.Contacts;
import com.company.vzvod.entity.MetroStation;
import com.company.vzvod.test_support.AuthenticatedAsAdmin;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.After;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@DisplayName("Интеграционный тест Contacts")
public class ContactsIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }


    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    private Contacts contacts;

    @BeforeAll
    static void startContainer() {
        postgreSQLContainer.start();
    }

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
        contacts = dataManager.create(Contacts.class);
        Address address = dataManager.create(Address.class);

        contacts.setPhoneNumber("89112291515");
        contacts.setHabitation(address);
        contacts.setRegistration(address);
        contacts.setNearestMetroStation(MetroStation.BALTIYSKAYA);
    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.end();
    }

    @Test
    void contextLoads() {
    }

    @Test
    @DisplayName("Тест сохранения в БД")
    void testSave() {
        Contacts savedContacts = dataManager.save(contacts);

        Contacts loadedContacts = dataManager.load(Contacts.class).id(savedContacts.getId()).one();

        assertEquals(loadedContacts.getHabitation().getId(), savedContacts.getHabitation().getId());
        assertEquals(loadedContacts.getPhoneNumber(), savedContacts.getPhoneNumber());
        assertEquals(loadedContacts.getNearestMetroStation(), savedContacts.getNearestMetroStation());
        assertEquals(loadedContacts.getRegistration().getId(), savedContacts.getRegistration().getId());

        assertEquals(MetroStation.BALTIYSKAYA, savedContacts.getNearestMetroStation());
        assertEquals("+79112291515", savedContacts.getPhoneNumber());
    }

    @Test
    @DisplayName("Тест изменения в БД")
    void testUpdate() {
        Contacts savedContacts = dataManager.save(contacts);
        UUID id = savedContacts.getId();

        Contacts loadedContacts = dataManager.load(Contacts.class).id(id).one();

        Address newHabitation = dataManager.create(Address.class);
        Address newRegistration = dataManager.create(Address.class);
        dataManager.save(newHabitation);
        dataManager.save(newRegistration);

        loadedContacts.setPhoneNumber("9995250228");
        loadedContacts.setNearestMetroStation(MetroStation.AKADEMICHESKAYA);
        loadedContacts.setHabitation(newHabitation);
        loadedContacts.setRegistration(newRegistration);

        Contacts updatedContacts = dataManager.save(loadedContacts);

        Contacts fromDb = dataManager.load(Contacts.class).id(id).one();

        assertEquals("+79995250228", fromDb.getPhoneNumber());
        assertEquals(MetroStation.AKADEMICHESKAYA, fromDb.getNearestMetroStation());
        assertEquals(newHabitation.getId(), fromDb.getHabitation().getId());
        assertEquals(newRegistration.getId(), fromDb.getRegistration().getId());
    }

    @Test
    @DisplayName("Тест удаления из БД")
    void testDelete() {
        Contacts saved = dataManager.save(contacts);
        UUID contactsId = saved.getId();

        dataManager.remove(saved);

        Contacts loaded = dataManager.load(Contacts.class)
                .id(contactsId)
                .optional()
                .orElse(null);

        assertNull(loaded);
    }
}
