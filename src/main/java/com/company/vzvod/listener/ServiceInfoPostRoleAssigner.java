package com.company.vzvod.listener;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.security.PostBasedRoleAssignmentService;
import com.company.vzvod.security.PostBasedRoleResolver;
import io.jmix.core.DataManager;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ServiceInfoPostRoleAssigner {

    private final DataManager dataManager;
    private final PostBasedRoleResolver roleResolver;
    private final PostBasedRoleAssignmentService roleAssignmentService;

    public ServiceInfoPostRoleAssigner(
            DataManager dataManager,
            PostBasedRoleResolver roleResolver,
            PostBasedRoleAssignmentService roleAssignmentService
    ) {
        this.dataManager = dataManager;
        this.roleResolver = roleResolver;
        this.roleAssignmentService = roleAssignmentService;
    }

    @EventListener
    public void onServiceInfoChangedBeforeCommit(EntityChangedEvent<ServiceInfo> event) {
        if (event.getType() == EntityChangedEvent.Type.DELETED) {
            return;
        }

        boolean shouldRecalc =
                event.getType() == EntityChangedEvent.Type.CREATED
                        || event.getChanges().isChanged("post");

        if (!shouldRecalc) {
            return;
        }

        ServiceInfo serviceInfo = dataManager.load(event.getEntityId()).one();
        if (serviceInfo.getUser() == null) {
            return;
        }

        String username = serviceInfo.getUser().getUsername();
        boolean shouldHaveFullAccess = roleResolver.shouldHaveFullAccess(serviceInfo.getPost());
        roleAssignmentService.ensurePostBasedRole(username, shouldHaveFullAccess);

        // drop deprecated roles if someone had them assigned in DB earlier
        roleAssignmentService.removeDeprecatedRoles(username, Set.of(
                "self-edit-user",
                "policeman-row-level",
                "app-admin"
        ));
    }
}

