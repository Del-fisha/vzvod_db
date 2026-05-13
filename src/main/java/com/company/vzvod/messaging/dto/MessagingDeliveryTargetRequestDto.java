package com.company.vzvod.messaging.dto;

import java.util.Set;
import java.util.UUID;

public record MessagingDeliveryTargetRequestDto(
        Set<UUID> recipientUserIds
) {
}
