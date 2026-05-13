package com.company.vzvod.messaging.dto;

import java.util.UUID;

public record MessagingDeliveryTargetDto(
        UUID userId,
        long chatId
) {
}
