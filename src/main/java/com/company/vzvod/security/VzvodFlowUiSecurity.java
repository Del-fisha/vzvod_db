package com.company.vzvod.security;

import io.jmix.security.util.JmixHttpSecurityUtils;
import io.jmix.securityflowui.security.FlowuiVaadinWebSecurity;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

/**
 * Vaadin/Jmix security. Skips Jmix concurrent HTTP session registry — with it enabled,
 * the browser can keep an old JSESSIONID while Spring invalidates the session (heartbeat 403).
 */
@Configuration
public class VzvodFlowUiSecurity extends FlowuiVaadinWebSecurity {

    @Override
    protected void configureJmixSpecifics(HttpSecurity http) throws Exception {
        JmixHttpSecurityUtils.configureAnonymous(http);
        JmixHttpSecurityUtils.configureRememberMe(http);
        JmixHttpSecurityUtils.configureFrameOptions(http);

        http.authorizeHttpRequests(urlRegistry -> {
            String loginPath = getLoginPath();
            urlRegistry.requestMatchers(request -> loginPath.equals(request.getRequestURI())).permitAll();

            MvcRequestMatcher.Builder mvcRequestMatcherBuilder = new MvcRequestMatcher.Builder(
                    applicationContext.getBean(HandlerMappingIntrospector.class));
            MvcRequestMatcher errorPageRequestMatcher = mvcRequestMatcherBuilder.pattern(
                    serverProperties.getError().getPath());
            urlRegistry.requestMatchers(errorPageRequestMatcher).permitAll();
        });

        initLoginView(http);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.csrf(AbstractHttpConfigurer::disable);
        http.sessionManagement(session -> session.sessionFixation(fixation -> fixation.none()));
    }
}
