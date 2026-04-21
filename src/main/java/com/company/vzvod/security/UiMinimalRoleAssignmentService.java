package com.company.vzvod.security;

import io.jmix.core.UnconstrainedDataManager;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UiMinimalRoleAssignmentService {

    private static final String ROLE_TYPE_RESOURCE = "resource";

    private final UnconstrainedDataManager dataManager;

    public UiMinimalRoleAssignmentService(UnconstrainedDataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void ensureAssigned(String username) {
        if (username == null || username.isBlank()) {
            return;
        }

        boolean exists = dataManager.load(RoleAssignmentEntity.class)
                .query("""
                        select ra from sec_RoleAssignmentEntity ra
                        where ra.username = :username
                          and ra.roleType = :roleType
                          and ra.roleCode = :roleCode
                        """)
                .parameter("username", username)
                .parameter("roleType", ROLE_TYPE_RESOURCE)
                .parameter("roleCode", UiMinimalRole.CODE)
                .maxResults(1)
                .optional()
                .isPresent();

        if (exists) {
            return;
        }

        RoleAssignmentEntity ra = dataManager.create(RoleAssignmentEntity.class);
        ra.setUsername(username);
        ra.setRoleType(ROLE_TYPE_RESOURCE);
        ra.setRoleCode(UiMinimalRole.CODE);

        dataManager.save(ra);
    }

    public List<String> loadAllUsernames() {
        return dataManager.loadValues("""
                        select u.username from User u
                        where u.username is not null
                        """)
                .properties("username")
                .list()
                .stream()
                .map(r -> (String) r.getValue("username"))
                .toList();
    }
}

