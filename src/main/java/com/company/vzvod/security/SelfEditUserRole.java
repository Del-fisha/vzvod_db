package com.company.vzvod.security;

import com.company.vzvod.entity.*;
import com.company.vzvod.view.shift.MyShiftListView;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(name = "Self edit User", code = SelfEditUserRole.CODE)
public interface SelfEditUserRole {
    String CODE = "self-edit-user";

    @EntityPolicy(entityClass = User.class, actions = {EntityPolicyAction.UPDATE})
    void userUpdate();

    @EntityPolicy(entityClass = ServiceInfo.class, actions = {EntityPolicyAction.UPDATE})
    void serviceInfoUpdate();

    @EntityPolicy(entityClass = IdCard.class, actions = {EntityPolicyAction.UPDATE})
    void idCardUpdate();

    @EntityPolicy(entityClass = Shift.class, actions = {EntityPolicyAction.CREATE, EntityPolicyAction.UPDATE})
    void shiftEntity();

    @EntityPolicy(entityClass = Vocation.class, actions = {EntityPolicyAction.CREATE, EntityPolicyAction.UPDATE})
    void vocationEntity();

    @EntityPolicy(entityClass = Contacts.class, actions = {EntityPolicyAction.CREATE, EntityPolicyAction.UPDATE})
    void contactsEntity();

    @EntityPolicy(entityClass = Address.class, actions = {EntityPolicyAction.CREATE, EntityPolicyAction.UPDATE})
    void addressEntity();

    @EntityPolicy(entityClass = Education.class, actions = {EntityPolicyAction.CREATE, EntityPolicyAction.UPDATE})
    void educationEntity();

    @EntityPolicy(entityClass = AdministrativeViolation.class, actions = {EntityPolicyAction.CREATE, EntityPolicyAction.UPDATE})
    void administrativeViolationEntity();

    @EntityPolicy(entityClass = CriminalViolation.class, actions = {EntityPolicyAction.CREATE, EntityPolicyAction.UPDATE})
    void criminalViolationEntity();

    @EntityPolicy(entityClass = Shift.class, actions = {EntityPolicyAction.READ, EntityPolicyAction.UPDATE})
    void shiftUpdate();

    @EntityAttributePolicy(entityClass = Shift.class, attributes = {"units"}, action = EntityAttributePolicyAction.MODIFY)
    void shiftUnitsModify();

    @EntityPolicy(entityClass = Event.class, actions = {EntityPolicyAction.READ, EntityPolicyAction.UPDATE})
    void eventEntity();

    @EntityAttributePolicy(
            entityClass = User.class,
            attributes = {"username", "password", "firstName", "lastName", "patronymic", "dateOfBirth"},
            action = EntityAttributePolicyAction.MODIFY
    )
    void userSelfFields();

    @EntityAttributePolicy(
            entityClass = ServiceInfo.class,
            attributes = {"token", "breastplate", "medicalExamination"},
            action = EntityAttributePolicyAction.MODIFY
    )
    void serviceInfoSelfFields();

    @EntityAttributePolicy(
            entityClass = IdCard.class,
            attributes = {"*"},
            action = EntityAttributePolicyAction.MODIFY
    )
    void idCardSelfFields();

    @EntityAttributePolicy(
            entityClass = Shift.class,
            attributes = "*",
            action = EntityAttributePolicyAction.MODIFY
    )
    void shiftSelfFields();

    @EntityAttributePolicy(
            entityClass = Vocation.class,
            attributes = "*",
            action = EntityAttributePolicyAction.MODIFY
    )
    void vocationSelfFields();

    @EntityAttributePolicy(
            entityClass = Contacts.class,
            attributes = {"*"},
            action = EntityAttributePolicyAction.MODIFY
    )
    void contactsSelfFields();

    @EntityAttributePolicy(
            entityClass = Address.class,
            attributes = "*",
            action = EntityAttributePolicyAction.MODIFY
    )
    void addressSelfFields();

    @EntityAttributePolicy(
            entityClass = Education.class,
            attributes = "*",
            action = EntityAttributePolicyAction.MODIFY
    )
    void educationSelfFields();

    @EntityAttributePolicy(
            entityClass = AdministrativeViolation.class,
            attributes = "*",
            action = EntityAttributePolicyAction.MODIFY
    )
    void administrativeViolationSelfFields();

    @EntityAttributePolicy(
            entityClass = CriminalViolation.class,
            attributes = "*",
            action = EntityAttributePolicyAction.MODIFY
    )
    void criminalViolationSelfFields();

    @EntityAttributePolicy(
            entityClass = Event.class,
            attributes = "*",
            action = EntityAttributePolicyAction.VIEW
    )
    void eventSelfFields();
}