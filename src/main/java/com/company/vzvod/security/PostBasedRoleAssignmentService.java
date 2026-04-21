package com.company.vzvod.security;

import io.jmix.core.UnconstrainedDataManager;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PostBasedRoleAssignmentService {

    private static final String ROLE_TYPE_RESOURCE = "resource";

    private final UnconstrainedDataManager dataManager;

    public PostBasedRoleAssignmentService(UnconstrainedDataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void ensurePostBasedRole(String username, boolean shouldHaveFullAccess) {
        if (username == null || username.isBlank()) {
            return;
        }

        ensureAssigned(username, UiMinimalRole.CODE);
        ensureAssigned(username, PolicemanRole.CODE);

        if (shouldHaveFullAccess) {
            ensureAssigned(username, FullAccessRole.CODE);
        } else {
            removeAssignmentIfExists(username, FullAccessRole.CODE);
        }
    }

    private void ensureAssigned(String username, String roleCode) {
        boolean exists = dataManager.load(RoleAssignmentEntity.class)
                .query("""
                        select ra from sec_RoleAssignmentEntity ra
                        where ra.username = :username
                          and ra.roleType = :roleType
                          and ra.roleCode = :roleCode
                        """)
                .parameter("username", username)
                .parameter("roleType", ROLE_TYPE_RESOURCE)
                .parameter("roleCode", roleCode)
                .maxResults(1)
                .optional()
                .isPresent();

        if (exists) {
            return;
        }

        RoleAssignmentEntity ra = dataManager.create(RoleAssignmentEntity.class);
        ra.setUsername(username);
        ra.setRoleType(ROLE_TYPE_RESOURCE);
        ra.setRoleCode(roleCode);
        dataManager.save(ra);
    }

    private void removeAssignmentIfExists(String username, String roleCode) {
        dataManager.load(RoleAssignmentEntity.class)
                .query("""
                        select ra from sec_RoleAssignmentEntity ra
                        where ra.username = :username
                          and ra.roleType = :roleType
                          and ra.roleCode = :roleCode
                        """)
                .parameter("username", username)
                .parameter("roleType", ROLE_TYPE_RESOURCE)
                .parameter("roleCode", roleCode)
                .list()
                .forEach(dataManager::remove);
    }

    public void removeDeprecatedRoles(String username, Set<String> roleCodes) {
        if (username == null || username.isBlank() || roleCodes == null || roleCodes.isEmpty()) {
            return;
        }
        for (String roleCode : roleCodes) {
            removeAssignmentIfExists(username, roleCode);
        }
    }
}

