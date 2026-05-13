package com.company.vzvod.messaging.dto;

import java.util.Set;
import java.util.UUID;

public record DashboardMessageDispatchRequest(
        UUID senderUserId,
        String senderDisplayName,
        String body,
        Set<UUID> recipientUserIds
) {
}
