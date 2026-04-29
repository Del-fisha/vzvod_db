package com.company.vzvod.notification;

import com.company.vzvod.entity.User;
import io.jmix.core.security.CurrentAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/internal/notifications")
public class InternalNotificationController {

    private static final Logger log = LoggerFactory.getLogger(InternalNotificationController.class);

    private final UserNotificationService userNotificationService;
    private final CurrentAuthentication currentAuthentication;
    private final String internalToken;

    public InternalNotificationController(
            UserNotificationService userNotificationService,
            CurrentAuthentication currentAuthentication,
            @Value("${internal.api.token:}") String internalToken
    ) {
        this.userNotificationService = userNotificationService;
        this.currentAuthentication = currentAuthentication;
        this.internalToken = internalToken;
    }

    @PostMapping("/overdue")
    public UUID createOverdue(
            @RequestHeader(name = "X-Internal-Token", required = false) String token,
            @RequestBody OverdueNotificationRequest request
    ) {
        log.info("Internal overdue request received: userId={}, itemsCount={}, tokenPresent={}",
                request == null ? null : request.userId(),
                request == null || request.items() == null ? 0 : request.items().size(),
                token != null && !token.isBlank());
        requireInternalToken(token);
        User createdBy = currentUserOrNull();
        UUID id = userNotificationService.createOverdueNotification(request, createdBy);
        log.info("Internal overdue created: notificationId={}", id);
        return id;
    }

    private User currentUserOrNull() {
        try {
            Object u = currentAuthentication.getUser();
            return (u instanceof User user) ? user : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private void requireInternalToken(String token) {
        if (internalToken == null || internalToken.isBlank()) {
            return; // dev mode
        }
        if (token == null || !internalToken.equals(token)) {
            log.warn("Internal token mismatch (configured={}, receivedPresent={})",
                    !internalToken.isBlank(), token != null && !token.isBlank());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
}

