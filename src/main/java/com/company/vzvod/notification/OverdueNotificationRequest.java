package com.company.vzvod.notification;

import java.util.List;
import java.util.UUID;

public record OverdueNotificationRequest(
        UUID userId,
        List<OverdueItemDto> items
) {
}

