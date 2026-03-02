package com.company.vzvod.integration;

import com.company.vzvod.entity.Address;
import com.company.vzvod.entity.Contacts;
import com.company.vzvod.entity.MetroStation;
import com.company.vzvod.test_support.AuthenticatedAsAdmin;
import io.jmix.core.DataManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
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

    private Contacts contacts;

    @BeforeAll
    static void startContainer() {
        postgreSQLContainer.start();
    }

    @BeforeEach
    void setUp() {
        contacts = dataManager.create(Contacts.class);
        Address address = dataManager.create(Address.class);
        Address savedAddress = dataManager.save(address);

        contacts.setPhoneNumber("89112291515");
        contacts.setHabitation(savedAddress);
        contacts.setRegistration(savedAddress);
        contacts.setNearestMetroStation(MetroStation.BALTIYSKAYA);
    }

    @Test
    void contextLoads() {
    }

    @Test
    @DisplayName("Тест сохранения в БД")
    void testSave() {
        Contacts savedContacts = dataManager.save(contacts);

        Contacts loadedContacts = dataManager.load(Contacts.class).id(savedContacts.getId()).one();

        assertEquals(loadedContacts.getHabitation(), savedContacts.getHabitation());
        assertEquals(loadedContacts.getPhoneNumber(), savedContacts.getPhoneNumber());
        assertEquals(loadedContacts.getNearestMetroStation(), savedContacts.getNearestMetroStation());
        assertEquals(loadedContacts.getRegistration(), savedContacts.getRegistration());

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
        assertEquals(newHabitation, fromDb.getHabitation());
        assertEquals(newRegistration, fromDb.getRegistration());
    }



}
