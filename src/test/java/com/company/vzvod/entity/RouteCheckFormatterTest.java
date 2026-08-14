package com.company.vzvod.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("RouteCheckFormatter")
class RouteCheckFormatterTest {

    @Test
    @DisplayName("время сверху, Фамилия И.О. строкой ниже без lower-case")
    void formatsCheckEntryTimeAboveCapitalFio() {
        String label = RouteCheckFormatter.formatEntry(LocalTime.of(14, 5), "Иванов И. О.");
        assertEquals("14:05\nИванов И. О.", label);
    }

    @Test
    @DisplayName("без ФИО — только время")
    void formatsTimeOnlyWhenNoFio() {
        assertEquals("14:05", RouteCheckFormatter.formatEntry(LocalTime.of(14, 5), "  "));
        assertEquals("14:05", RouteCheckFormatter.formatEntry(LocalTime.of(14, 5), null));
    }

    @Test
    @DisplayName("склеивает несколько проверок через разделитель")
    void joinsMultipleEntries() {
        String joined = RouteCheckFormatter.joinEntries(List.of(
                "10:15\nИванов И. О.",
                "14:30\nПетров П. С."
        ));
        assertEquals("10:15\nИванов И. О. — 14:30\nПетров П. С.", joined);
    }

    @Test
    @DisplayName("пустой список даёт тире")
    void emptyListIsDash() {
        assertEquals("—", RouteCheckFormatter.joinEntries(List.of()));
    }
}
