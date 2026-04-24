package com.company.vzvod.security;

import com.company.vzvod.entity.User;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.security.SystemAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

@Profile("test")
@Component
public class TestAdminPasswordEnsurer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TestAdminPasswordEnsurer.class);

    private static final UUID SEEDED_ADMIN_ID = UUID.fromString("60885987-1b61-4247-94c7-dff348347f93");

    private final UnconstrainedDataManager dataManager;
    private final SystemAuthenticator systemAuthenticator;
    private final PasswordEncoder passwordEncoder;
    private final DataSource dataSource;

    public TestAdminPasswordEnsurer(
            UnconstrainedDataManager dataManager,
            SystemAuthenticator systemAuthenticator,
            PasswordEncoder passwordEncoder,
            DataSource dataSource
    ) {
        this.dataManager = dataManager;
        this.systemAuthenticator = systemAuthenticator;
        this.passwordEncoder = passwordEncoder;
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        systemAuthenticator.begin();
        try {
            User admin = dataManager.load(User.class)
                    .id(SEEDED_ADMIN_ID)
                    .optional()
                    .orElse(null);

            if (admin == null) {
                log.warn("Seeded admin user not found (id={})", SEEDED_ADMIN_ID);
                return;
            }

            String currentHash = admin.getPassword();
            boolean matches = currentHash != null && passwordEncoder.matches("admin", currentHash);
            log.info("Test profile admin password matches: {}, encoder={}", matches, passwordEncoder.getClass().getName());
            if (matches) {
                return;
            }

            // Most robust for dev/test logins: keep explicit {noop} seed-style password.
            // (Works with DelegatingPasswordEncoder and keeps the DB readable.)
            updatePasswordDirectly("{noop}admin");
        } finally {
            systemAuthenticator.end();
        }
    }

    private void updatePasswordDirectly(String newHash) {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE USER_ SET PASSWORD = ? WHERE ID = ?")) {
            ps.setString(1, newHash);
            // HSQLDB treats UUID columns well with string values too.
            ps.setString(2, SEEDED_ADMIN_ID.toString());
            int updated = ps.executeUpdate();
            log.info("Updated admin password rows: {}", updated);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update admin password in test profile", e);
        }
    }
}

