package com.company.vzvod.security;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import org.springframework.stereotype.Component;

/**
 * Logs session creation/destruction and logout to diagnose unexpected logouts.
 */
@Component
public class SessionLogoutDiagnostics implements HttpSessionListener {

    private static final Logger log = LoggerFactory.getLogger(SessionLogoutDiagnostics.class);

    @EventListener
    public void onLogoutSuccess(LogoutSuccessEvent event) {
        String user = event.getAuthentication() != null ? event.getAuthentication().getName() : "?";
        log.warn("LogoutSuccessEvent: user={}", user);
    }

    @EventListener
    public void onHttpSessionDestroyed(HttpSessionDestroyedEvent event) {
        log.warn("HttpSessionDestroyed (Spring Security): id={}, securityContexts={}",
                event.getSession() != null ? event.getSession().getId() : "?",
                event.getSecurityContexts() != null ? event.getSecurityContexts().size() : 0);
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        log.info("HttpSessionListener.sessionCreated: id={}, maxInactiveInterval={}s",
                session.getId(), session.getMaxInactiveInterval());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        log.warn("HttpSessionListener.sessionDestroyed: id={}, maxInactiveInterval={}s",
                session.getId(), session.getMaxInactiveInterval(), new Exception("sessionDestroyed"));
    }
}
