package com.company.vzvod.security;

import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UiMinimalRoleAssignmentServiceTest {

    @Autowired
    UiMinimalRoleAssignmentService service;

    @Autowired
    DataManager dataManager;

    @Autowired
    SystemAuthenticator systemAuthenticator;

    @BeforeEach
    void begin() {
        systemAuthenticator.begin();
    }

    @AfterEach
    void end() {
        systemAuthenticator.end();
    }

    @Test
    void ensureAssigned_createsDefaultAssignments() {
        String username = "admino";

        // precondition: may or may not exist, but after ensureAssigned it must exist.
        service.ensureAssigned(username);

        boolean uiMinimalExists = dataManager.load(RoleAssignmentEntity.class)
                .query("""
                        select ra from sec_RoleAssignmentEntity ra
                        where ra.username = :username
                          and ra.roleType = :roleType
                          and ra.roleCode = :roleCode
                        """)
                .parameter("username", username)
                .parameter("roleType", "resource")
                .parameter("roleCode", UiMinimalRole.CODE)
                .maxResults(1)
                .optional()
                .isPresent();

        boolean policemanExists = dataManager.load(RoleAssignmentEntity.class)
                .query("""
                        select ra from sec_RoleAssignmentEntity ra
                        where ra.username = :username
                          and ra.roleType = :roleType
                          and ra.roleCode = :roleCode
                        """)
                .parameter("username", username)
                .parameter("roleType", "resource")
                .parameter("roleCode", PolicemanRole.CODE)
                .maxResults(1)
                .optional()
                .isPresent();

        assertTrue(uiMinimalExists);
        assertTrue(policemanExists);
    }
}

