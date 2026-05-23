package com.company.vzvod.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Logs heartbeat failures (403) with cookie vs server session state.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class Heartbeat403DiagnosticsFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(Heartbeat403DiagnosticsFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        filterChain.doFilter(request, response);
        if (!isHeartbeat(request) || response.getStatus() != HttpServletResponse.SC_FORBIDDEN) {
            return;
        }
        HttpSession session = request.getSession(false);
        String requestedSessionId = request.getRequestedSessionId();
        String cookieSessionId = sessionIdFromCookies(request);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String user = auth != null ? auth.getName() : "null";
        log.warn(
                "Heartbeat returned 403. cookieSessionId={}, requestedSessionId={}, serverSessionId={}, "
                        + "sessionValid={}, sessionMaxInactiveInterval={}s, authenticated={}, user={}",
                cookieSessionId,
                requestedSessionId,
                session != null ? session.getId() : "no-session",
                session != null && requestedSessionId != null && requestedSessionId.equals(session.getId()),
                session != null ? session.getMaxInactiveInterval() : -1,
                auth != null && auth.isAuthenticated(),
                user
        );
    }

    private static String sessionIdFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return "no-cookies";
        }
        return Arrays.stream(cookies)
                .filter(c -> "JSESSIONID".equals(c.getName()) || "VZVOD_JSESSIONID".equals(c.getName()))
                .map(c -> c.getName() + "=" + c.getValue())
                .collect(Collectors.joining(", "));
    }

    private static boolean isHeartbeat(HttpServletRequest request) {
        return "heartbeat".equals(request.getParameter("v-r"));
    }
}
