package com.company.vzvod.notification;

import java.time.LocalDate;

public record OverdueItemDto(
        OverdueItemType type,
        LocalDate date
) {
}

