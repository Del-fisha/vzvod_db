package com.company.vzvod.dashboard.todayshift;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("RouteCheckChipParts — время сверху, ФИО снизу")
class RouteCheckChipPartsTest {

    @Test
    @DisplayName("разделяет время и ФИО для двухстрочного чипа")
    void splitsTimeAndName() {
        RouteCheckChipParts parts = RouteCheckChipParts.of(LocalTime.of(21, 28), "Тарасов А. Н.");
        assertEquals("21:28", parts.time());
        assertEquals("Тарасов А. Н.", parts.name());
    }

    @Test
    @DisplayName("без ФИО имя пустое")
    void emptyNameWhenNoFio() {
        RouteCheckChipParts parts = RouteCheckChipParts.of(LocalTime.of(21, 28), "  ");
        assertEquals("21:28", parts.time());
        assertEquals("", parts.name());
    }

    @Test
    @DisplayName("время и ФИО — разные части (не одна строка)")
    void timeAndNameAreSeparateParts() {
        RouteCheckChipParts parts = RouteCheckChipParts.of(LocalTime.of(21, 28), "Тарасов А. Н.");
        assertEquals(-1, parts.time().indexOf(parts.name()));
        assertEquals(-1, parts.name().indexOf(parts.time()));
        assertEquals("21:28", parts.time());
        assertEquals("Тарасов А. Н.", parts.name());
    }
}
