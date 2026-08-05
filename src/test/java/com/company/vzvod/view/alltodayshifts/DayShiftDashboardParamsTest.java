package com.company.vzvod.view.alltodayshifts;

import com.company.vzvod.entity.Dep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Параметры DayShiftDashboard")
class DayShiftDashboardParamsTest {

    @Test
    @DisplayName("Парсит date и department из query")
    void parse_validParams() {
        Optional<DayShiftDashboardParams> parsed = DayShiftDashboardParams.from(Map.of(
                "date", List.of("2026-05-13"),
                "department", List.of("2")
        ));

        assertTrue(parsed.isPresent());
        assertEquals(LocalDate.of(2026, 5, 13), parsed.get().date());
        assertEquals(Dep.SECOND, parsed.get().department());
    }

    @Test
    @DisplayName("Пусто при отсутствии параметров")
    void parse_missingParams() {
        assertTrue(DayShiftDashboardParams.from(Map.of()).isEmpty());
        assertTrue(DayShiftDashboardParams.from(Map.of("date", List.of("2026-05-13"))).isEmpty());
        assertTrue(DayShiftDashboardParams.from(Map.of("department", List.of("1"))).isEmpty());
    }

    @Test
    @DisplayName("Пусто при невалидных значениях")
    void parse_invalidParams() {
        assertTrue(DayShiftDashboardParams.from(Map.of(
                "date", List.of("not-a-date"),
                "department", List.of("1")
        )).isEmpty());
        assertTrue(DayShiftDashboardParams.from(Map.of(
                "date", List.of("2026-05-13"),
                "department", List.of("99")
        )).isEmpty());
    }
}
