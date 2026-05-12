package com.company.vzvod.security;

import io.jmix.core.security.CurrentAuthentication;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UiAccessService {

    @Autowired
    private CurrentAuthentication currentAuthentication;

    @Autowired
    private RoleGrantedAuthorityUtils roleGrantedAuthorityUtils;

    public boolean hasFullAccessRole() {
        return hasRole(FullAccessRole.CODE);
    }

    public boolean hasRole(String roleCode) {
        String prefix = roleGrantedAuthorityUtils.getDefaultRolePrefix();
        return currentAuthentication.getUser().getAuthorities().stream()
                .anyMatch(grantedAuthority -> {
                    String a = grantedAuthority.getAuthority();
                    return roleCode.equals(a) || (prefix + roleCode).equals(a);
                });
    }
}

