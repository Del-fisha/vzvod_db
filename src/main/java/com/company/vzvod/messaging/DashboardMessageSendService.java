package com.company.vzvod.messaging;

import com.company.vzvod.entity.User;
import com.company.vzvod.messaging.client.NotificationServiceClient;
import com.company.vzvod.messaging.dto.DashboardMessageDispatchRequest;
import com.company.vzvod.security.UiAccessService;
import com.company.vzvod.service.ShiftOperationalDay;
import io.jmix.core.security.CurrentAuthentication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.UUID;

@Service
public class DashboardMessageSendService {

    private final UiAccessService uiAccessService;
    private final CurrentAuthentication currentAuthentication;
    private final DashboardMessageRecipientResolver recipientResolver;
    private final NotificationServiceClient notificationServiceClient;

    public DashboardMessageSendService(
            UiAccessService uiAccessService,
            CurrentAuthentication currentAuthentication,
            DashboardMessageRecipientResolver recipientResolver,
            NotificationServiceClient notificationServiceClient
    ) {
        this.uiAccessService = uiAccessService;
        this.currentAuthentication = currentAuthentication;
        this.recipientResolver = recipientResolver;
        this.notificationServiceClient = notificationServiceClient;
    }

    public void send(DashboardMessageAudience audience, String messageBody) {
        if (!uiAccessService.hasFullAccessRole()) {
            throw new AccessDeniedException("Dashboard messaging requires FullAccessRole");
        }
        if (messageBody == null || messageBody.isBlank()) {
            throw new IllegalArgumentException("Message body is empty");
        }

        User sender = (User) currentAuthentication.getUser();
        LocalDateTime now = LocalDateTime.now();
        Set<UUID> recipientUserIds = recipientResolver.resolve(
                audience,
                sender.getId(),
                ShiftOperationalDay.resolveOperationalDate(now, ZoneId.systemDefault())
        );

        DashboardMessageDispatchRequest request = new DashboardMessageDispatchRequest(
                sender.getId(),
                sender.getShortFio(),
                messageBody,
                recipientUserIds
        );
        notificationServiceClient.sendDashboardMessage(request);
    }
}
