package com.company.vzvod.security;

import com.company.vzvod.entity.User;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(name = "PolicemanRole", code = PolicemanRole.CODE)
public interface PolicemanRole {
    String CODE = "policeman-role";

    @EntityPolicy(entityClass = User.class, actions = {EntityPolicyAction.READ, EntityPolicyAction.UPDATE})
    void userEntity();


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
}