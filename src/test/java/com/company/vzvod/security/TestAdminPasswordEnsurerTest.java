package com.company.vzvod.security;

import com.company.vzvod.entity.User;
import io.jmix.core.UnconstrainedDataManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TestAdminPasswordEnsurerTest {

    private static final UUID SEEDED_ADMIN_ID = UUID.fromString("60885987-1b61-4247-94c7-dff348347f93");

    @Autowired
    UnconstrainedDataManager dataManager;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void adminPassword_isAdmin_inTestProfile() {
        User admin = dataManager.load(User.class)
                .id(SEEDED_ADMIN_ID)
                .one();

        assertNotNull(admin.getPassword());
        assertTrue(passwordEncoder.matches("admin", admin.getPassword()));
    }
}

