package com.company.vzvod.security;

import com.company.vzvod.entity.User;
import io.jmix.security.role.annotation.JpqlRowLevelPolicy;
import io.jmix.security.role.annotation.RowLevelRole;

@RowLevelRole(name = "PolicemanRowLevel", code = PolicemanRowLevelRole.CODE)
public interface PolicemanRowLevelRole {
    String CODE = "policeman-row-level";

    @JpqlRowLevelPolicy(
            entityClass = User.class,
            where = "{E}.id = :current_user_id"

    )
    void userUpdateOnlySelf();

}