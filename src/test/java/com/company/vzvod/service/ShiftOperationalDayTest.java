package com.company.vzvod.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Операционные сутки смены")
class ShiftOperationalDayTest {

    private static final ZoneId ZONE = ZoneId.of("Europe/Moscow");

    @Test
    @DisplayName("До 04:00 относится к предыдущему календарному дню")
    void beforeRolloverUsesPreviousCalendarDay() {
        LocalDateTime wallClock = LocalDateTime.of(2026, 5, 13, 3, 59);

        assertEquals(LocalDate.of(2026, 5, 12), ShiftOperationalDay.resolveOperationalDate(wallClock, ZONE));
    }

    @Test
    @DisplayName("С 04:00 относится к текущему календарному дню")
    void fromRolloverUsesCurrentCalendarDay() {
        LocalDateTime wallClock = LocalDateTime.of(2026, 5, 13, 4, 0);

        assertEquals(LocalDate.of(2026, 5, 13), ShiftOperationalDay.resolveOperationalDate(wallClock, ZONE));
    }
}
