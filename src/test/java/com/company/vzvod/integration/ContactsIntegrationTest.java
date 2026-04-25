package com.company.vzvod.integration;

import com.company.vzvod.entity.Address;
import com.company.vzvod.entity.Contacts;
import com.company.vzvod.entity.MetroStation;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Интеграционный тест Contacts")
public class ContactsIntegrationTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    private Contacts contacts;
    private UUID createdUserId;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();

        User user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);
        user = dataManager.save(user);
        createdUserId = user.getId();

        contacts = dataManager.create(Contacts.class);
        Address address = dataManager.create(Address.class);

        contacts.setUser(user);
        contacts.setPhoneNumber("89112291515");
        contacts.setHabitation(address);
        contacts.setRegistration(address);
        contacts.setNearestMetroStation(MetroStation.BALTIYSKAYA);
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
