package com.company.vzvod.bot;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StaffWorkPeriodsTest {

    @Test
    void monthStartsOnTheFirstAndYearOnJanuary() {
        LocalDate today = LocalDate.of(2026, 11, 10);
        assertEquals(LocalDate.of(2026, 11, 1), StaffWorkPeriods.month(today).from());
        assertEquals(LocalDate.of(2026, 11, 30), StaffWorkPeriods.month(today).to());
        assertEquals(LocalDate.of(2026, 1, 1), StaffWorkPeriods.year(today).from());
        assertEquals(LocalDate.of(2026, 12, 31), StaffWorkPeriods.year(today).to());
    }

    @Test
    void februaryOfLeapYearEndsOnThe29th() {
        assertEquals(LocalDate.of(2024, 2, 29), StaffWorkPeriods.month(LocalDate.of(2024, 2, 10)).to());
    }
}
