package com.company.vzvod.security;

import com.company.vzvod.entity.*;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Application Admin", code = ApplicationAdminRole.CODE, scope= "UI")
public interface ApplicationAdminRole {
    String CODE = "app-admin";

    @MenuPolicy(
            menuIds = {
                    "User.list", "Penalty.list", "Incentive.list", "Vocation.list",
                    "AdministrativeViolation.list", "Shift.list", "CriminalViolation.list",
                    "UserListView"}
    )
    @ViewPolicy(
            viewIds = {
                    "MainViewTopMenu",
                    "User.list",
                    "Penalty.list",
                    "Incentive.list",
                    "Vocation.list",
                    "AdministrativeViolation.list",
                    "Shift.list",
                    "CriminalViolation.list",
                    "UserListView"
            }
    )
    void screens();

    // --- Полные права на сущности ---

    @EntityPolicy(
            entityClass = User.class,
            actions = EntityPolicyAction.READ       // READ, CREATE, UPDATE, DELETE
    )
    @EntityAttributePolicy(
            entityClass = User.class,
            attributes = "*",                      // все поля
            action = EntityAttributePolicyAction.MODIFY // можно читать и изменять
    )
    void user();

    @EntityPolicy(
            entityClass = ServiceInfo.class,
            actions = EntityPolicyAction.ALL
    )
    @EntityAttributePolicy(
            entityClass = ServiceInfo.class,
            attributes = "*",
            action = EntityAttributePolicyAction.MODIFY
    )
    void serviceInfo();

    @EntityPolicy(
            entityClass = Penalty.class,
            actions = EntityPolicyAction.ALL
    )
    @EntityAttributePolicy(
            entityClass = Penalty.class,
            attributes = "*",
            action = EntityAttributePolicyAction.MODIFY
    )
    void penalty();

    @EntityPolicy(
            entityClass = Incentive.class,
            actions = EntityPolicyAction.ALL
    )
    @EntityAttributePolicy(
            entityClass = Incentive.class,
            attributes = "*",
            action = EntityAttributePolicyAction.MODIFY
    )
    void incentive();

    @EntityPolicy(
            entityClass = Vocation.class,
            actions = EntityPolicyAction.ALL
    )
    @EntityAttributePolicy(
            entityClass = Vocation.class,
            attributes = "*",
            action = EntityAttributePolicyAction.MODIFY
    )
    void vocation();

    @EntityPolicy(
            entityClass = Shift.class,
            actions = EntityPolicyAction.ALL
    )
    @EntityAttributePolicy(
            entityClass = Shift.class,
            attributes = "*",
            action = EntityAttributePolicyAction.MODIFY
    )
    void shift();

    @EntityPolicy(
            entityClass = AdministrativeViolation.class,
            actions = EntityPolicyAction.ALL
    )
    @EntityAttributePolicy(
            entityClass = AdministrativeViolation.class,
            attributes = "*",
            action = EntityAttributePolicyAction.MODIFY
    )
    void administrativeViolation();

    @EntityPolicy(
            entityClass = CriminalViolation.class,
            actions = EntityPolicyAction.ALL
    )
    @EntityAttributePolicy(
            entityClass = CriminalViolation.class,
            attributes = "*",
            action = EntityAttributePolicyAction.MODIFY
    )
    void criminalViolation();
}