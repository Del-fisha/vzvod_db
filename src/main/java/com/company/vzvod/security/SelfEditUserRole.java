package com.company.vzvod.security;

import com.company.vzvod.entity.*;
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

    @EntityPolicy(entityClass = Contacts.class, actions = {EntityPolicyAction.CREATE, EntityPolicyAction.UPDATE})
    void contactsEntity();

    @EntityPolicy(entityClass = Address.class, actions = {EntityPolicyAction.CREATE, EntityPolicyAction.UPDATE})
    void addressEntity();

    @EntityPolicy(entityClass = Education.class, actions = {EntityPolicyAction.CREATE, EntityPolicyAction.UPDATE})
    void educationEntity();

//    @EntityPolicy(entityClass = Event.class, actions = {EntityPolicyAction.READ, EntityPolicyAction.UPDATE})
//    void eventEntity();

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

//    @EntityAttributePolicy(
//            entityClass = Event.class,
//            attributes = "*",
//            action = EntityAttributePolicyAction.VIEW
//    )
//    void eventSelfFields();
}