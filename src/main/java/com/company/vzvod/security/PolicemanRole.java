package com.company.vzvod.security;

import com.company.vzvod.entity.*;
import com.company.vzvod.view.shift.MyShiftListView;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "PolicemanRole", code = PolicemanRole.CODE, scope = SecurityScope.UI)
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

    @EntityPolicy(entityClass = Vocation.class, actions = {EntityPolicyAction.READ})
    void vocationEntity();

    @EntityPolicy(entityClass = Contacts.class, actions = {EntityPolicyAction.READ})
    void contactsEntity();

    @EntityPolicy(entityClass = Address.class, actions = {EntityPolicyAction.READ})
    void addressEntity();

    @EntityPolicy(entityClass = Education.class, actions = {EntityPolicyAction.READ})
    void educationEntity();

    @EntityPolicy(entityClass = AdministrativeViolation.class, actions = {EntityPolicyAction.READ})
    void administrativeViolationEntity();

    @EntityPolicy(entityClass = CriminalViolation.class, actions = {EntityPolicyAction.READ})
    void criminalViolationEntity();

    @EntityPolicy(entityClass = Event.class, actions = {EntityPolicyAction.READ})
    void eventEntity();


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
    void shiftSelfFields();

    @EntityAttributePolicy(
            entityClass = Vocation.class,
            attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void vocationTypeSelfFields();

    @EntityAttributePolicy(
            entityClass = Contacts.class,
            attributes = "*",
            action = EntityAttributePolicyAction.VIEW
    )
    void contactsTypeSelfFields();

    @EntityAttributePolicy(
            entityClass = Address.class,
            attributes = "*",
            action = EntityAttributePolicyAction.VIEW
    )
    void addressTypeSelfFields();

    @EntityAttributePolicy(
            entityClass = Education.class,
            attributes = "*",
            action = EntityAttributePolicyAction.VIEW
    )
    void educationTypeSelfFields();

    @EntityAttributePolicy(
            entityClass = AdministrativeViolation.class,
            attributes = "*",
            action = EntityAttributePolicyAction.VIEW
    )
    void administrativeViolationSelfFields();

    @EntityAttributePolicy(
            entityClass = CriminalViolation.class,
            attributes = "*",
            action = EntityAttributePolicyAction.VIEW
    )
    void criminalViolationSelfFields();

    @EntityAttributePolicy(
            entityClass = Event.class,
            attributes = "*",
            action = EntityAttributePolicyAction.VIEW
    )
    void eventSelfFields();

    @ViewPolicy(viewIds = {
            "User.list",
            "MainView",
            "User.detail",
            "UserListView",
            "UserCardView",
            "ServiceInfo.list",
            "ServiceInfo.detail",
            "IdCard.detail",
            "Penalty.detail",
            "Penalty.list",
            "Incentive.detail",
            "Incentive.list",
            "Shift.detail",
//            "Shift.list",
            "Vocation.list",
            "Vocation.detail",
            "Contacts.detail",
            "Address.detail",
            "Education.detail",
            "UserCardView",
            "ShiftBlankView",
            "AdministrativeViolation.detail",
            "AdministrativeViolation.list",
            "ProfileRedirect",
            "CriminalViolation.detail",
            "CriminalViolation.list",
            "MyShift.list",
            "Event.list",
            "LastEvent.list"
//            "Event.detail",
//            "DeletedEvent.detail",
//            "DeletedEvent.list"
    })
    @MenuPolicy(menuIds = {
            "all_employees_to_read",
            "my_profile",
            "my_shifts",
            "future_events",
            "last_events"
    })
    void userViews();
}