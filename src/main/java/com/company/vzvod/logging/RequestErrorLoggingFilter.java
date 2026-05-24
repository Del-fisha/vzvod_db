package com.company.vzvod.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class RequestErrorLoggingFilter extends OncePerRequestFilter {

    private final LoggingServiceClient loggingClient;

    public RequestErrorLoggingFilter(LoggingServiceClient loggingClient) {
        this.loggingClient = loggingClient;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            loggingClient.logError("Ошибка запроса %s %s: %s"
                    .formatted(request.getMethod(), request.getRequestURI(), exception.getMessage()));
            throw exception;
        }
    }
}
