package com.company.vzvod.security;

import com.company.vzvod.entity.IdCard;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
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


}