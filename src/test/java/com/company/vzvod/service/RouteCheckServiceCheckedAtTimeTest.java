package com.company.vzvod.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("RouteCheckService: время проверки")
class RouteCheckServiceCheckedAtTimeTest {

    @Test
    @DisplayName("фиксирует локальное время Europe/Moscow, не UTC")
    void nowCheckedAt_usesMoscowZone() {
        // 2026-08-10 18:14:30 UTC == 21:14:30 Europe/Moscow
        Clock utcClock = Clock.fixed(Instant.parse("2026-08-10T18:14:30Z"), ZoneId.of("UTC"));
        LocalTime moscow = RouteCheckService.nowCheckedAt(utcClock.withZone(RouteCheckService.MOSCOW));
        assertEquals(LocalTime.of(21, 14), moscow);
    }
}
