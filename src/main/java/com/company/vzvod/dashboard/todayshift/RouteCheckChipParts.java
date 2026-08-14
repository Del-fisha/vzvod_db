package com.company.vzvod.dashboard.todayshift;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Части чипа проверки: время и ФИО отдельными строками UI.
 */
public record RouteCheckChipParts(String time, String name) {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    public static RouteCheckChipParts of(LocalTime checkedAt, String checkerShortFio) {
        String time = checkedAt == null ? "—" : TIME.format(checkedAt);
        String name = checkerShortFio == null ? "" : checkerShortFio.trim();
        return new RouteCheckChipParts(time, name);
    }
}
