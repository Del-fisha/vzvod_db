package com.company.vzvod.security;

import com.company.vzvod.entity.*;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "PolicemanRole", code = PolicemanRole.CODE)
public interface PolicemanRole {
    String CODE = "policeman-role";

    @EntityPolicy(entityClass = User.class, actions = {EntityPolicyAction.READ})
    void userEntity();

    @EntityPolicy(entityClass = ServiceInfo.class, actions = {EntityPolicyAction.READ})
    void serviceEntity();

    @EntityPolicy(entityClass = IdCard.class, actions = {EntityPolicyAction.READ})
    void idCardEntity();

    @EntityPolicy(entityClass = Penalty.class, actions = {EntityPolicyAction.READ})
    void penaltyEntity();

    @EntityPolicy(entityClass = Incentive.class, actions = {EntityPolicyAction.READ})
    void incentiveEntity();

    @EntityPolicy(entityClass = Shift.class, actions = {EntityPolicyAction.READ})
    void shiftEntity();

    @EntityAttributePolicy(
            entityClass = User.class,
            attributes = {"firstName", "lastName", "patronymic", "dateOfBirth"},
            action = EntityAttributePolicyAction.MODIFY
    )
    void userCommonFieldsView();

    @EntityAttributePolicy(
            entityClass = User.class,
            attributes = {"username", "password"},
            action = EntityAttributePolicyAction.MODIFY
    )
    void userCredentials();

    @EntityAttributePolicy(
            entityClass = ServiceInfo.class,
            attributes = {"*"},
            action = EntityAttributePolicyAction.VIEW
    )
    void serviceInfoSelfFields();

    @EntityAttributePolicy(
            entityClass = IdCard.class,
            attributes = {"*"},
            action = EntityAttributePolicyAction.VIEW
    )
    void idCardSelfFields();

    @EntityAttributePolicy(
            entityClass = Penalty.class,
            attributes = {"*"},
            action = EntityAttributePolicyAction.VIEW
    )
    void penaltySelfFields();

    @EntityAttributePolicy(
            entityClass = Incentive.class,
            attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void incentiveSelfFields();

    @EntityAttributePolicy(
            entityClass = Shift.class,
            attributes = "*",
            action = EntityAttributePolicyAction.VIEW
    )

    @ViewPolicy(viewIds = {
            "User.list",
            "User.detail",
            "UserListView",
            "UserCardView",
            "ServiceInfo.detail",
            "IdCard.detail",
            "Penalty.detail",
            "Penalty.list",
            "Incentive.detail",
            "Incentive.list",
            "Shift.detail",
            "Shift.list"
    })
    @MenuPolicy(menuIds = {
            "User.list",
            "UserListView"
    })
    void userViews();
}