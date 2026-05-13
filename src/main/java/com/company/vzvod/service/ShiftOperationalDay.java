package com.company.vzvod.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

public final class ShiftOperationalDay {

    private static final LocalTime ROLLOVER = LocalTime.of(4, 0);

    private ShiftOperationalDay() {
    }

    public static LocalDate resolveOperationalDate(LocalDateTime wallClock) {
        return resolveOperationalDate(wallClock, ZoneId.systemDefault());
    }

    public static LocalDate resolveOperationalDate(LocalDateTime wallClock, ZoneId zoneId) {
        LocalDateTime zoned = wallClock.atZone(zoneId).toLocalDateTime();
        if (zoned.toLocalTime().isBefore(ROLLOVER)) {
            return zoned.toLocalDate().minusDays(1);
        }
        return zoned.toLocalDate();
    }
}
