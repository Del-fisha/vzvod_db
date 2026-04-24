package com.company.vzvod.security;

import io.jmix.securityflowui.security.FlowuiVaadinWebSecurity;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Configuration
public class VaadinWebSecurityConfiguration extends FlowuiVaadinWebSecurity {

    private final PostBasedRoleRefreshFilter postBasedRoleRefreshFilter;

    public VaadinWebSecurityConfiguration(PostBasedRoleRefreshFilter postBasedRoleRefreshFilter) {
        this.postBasedRoleRefreshFilter = postBasedRoleRefreshFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.addFilterAfter(postBasedRoleRefreshFilter, SecurityContextHolderFilter.class);
    }
}

