package com.company.vzvod.security;

import io.jmix.core.security.SystemAuthenticator;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class UiMinimalRoleBackfillRunner implements ApplicationRunner {

    private final UiMinimalRoleAssignmentService uiMinimalRoleAssignmentService;
    private final SystemAuthenticator systemAuthenticator;

    public UiMinimalRoleBackfillRunner(
            UiMinimalRoleAssignmentService uiMinimalRoleAssignmentService,
            SystemAuthenticator systemAuthenticator
    ) {
        this.uiMinimalRoleAssignmentService = uiMinimalRoleAssignmentService;
        this.systemAuthenticator = systemAuthenticator;
    }

    @Override
    public void run(ApplicationArguments args) {
        systemAuthenticator.begin();
        try {
            for (String username : uiMinimalRoleAssignmentService.loadAllUsernames()) {
                uiMinimalRoleAssignmentService.ensureAssigned(username);
            }
        } finally {
            systemAuthenticator.end();
        }
    }
}

