package com.company.vzvod.listener;

import com.company.vzvod.entity.User;
import com.company.vzvod.security.UiMinimalRoleAssignmentService;
import io.jmix.core.DataManager;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class UserUiMinimalRoleAssigner {

    private final DataManager dataManager;
    private final UiMinimalRoleAssignmentService uiMinimalRoleAssignmentService;

    public UserUiMinimalRoleAssigner(
            DataManager dataManager,
            UiMinimalRoleAssignmentService uiMinimalRoleAssignmentService
    ) {
        this.dataManager = dataManager;
        this.uiMinimalRoleAssignmentService = uiMinimalRoleAssignmentService;
    }

    @EventListener
    public void onUserChangedBeforeCommit(EntityChangedEvent<User> event) {
        if (event.getType() == EntityChangedEvent.Type.DELETED) {
            return;
        }

        boolean shouldEnsure =
                event.getType() == EntityChangedEvent.Type.CREATED
                        || event.getChanges().isChanged("username");

        if (!shouldEnsure) {
            return;
        }

        User user = dataManager.load(event.getEntityId()).one();
        String username = user.getUsername();

        uiMinimalRoleAssignmentService.ensureAssigned(username);
    }
}

