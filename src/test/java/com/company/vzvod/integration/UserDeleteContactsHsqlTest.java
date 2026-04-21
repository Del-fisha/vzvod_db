package com.company.vzvod.integration;

import com.company.vzvod.entity.Contacts;
import com.company.vzvod.entity.ServiceInfo;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UserDeleteContactsHsqlTest {

    @Autowired
    DataManager dataManager;

    @Autowired
    SystemAuthenticator systemAuthenticator;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("Удаление User каскадом удаляет Contacts (HSQL profile)")
    void deletingUser_shouldDeleteContacts() {
        User user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);

        ServiceInfo serviceInfo = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(serviceInfo);
        serviceInfo.setUser(user);
        user.setServiceInfo(serviceInfo);

        Contacts contacts = dataManager.create(Contacts.class);
        contacts.setUser(user);
        user.setContactsInfo(contacts);

        User saved = dataManager.save(user);
        UUID userId = saved.getId();
        UUID contactsId = saved.getContactsInfo().getId();

        assertNotNull(userId);
        assertNotNull(contactsId);

        dataManager.remove(saved);

        assertTrue(dataManager.load(User.class).id(userId).optional().isEmpty());
        assertTrue(dataManager.load(Contacts.class).id(contactsId).optional().isEmpty());
    }
}

