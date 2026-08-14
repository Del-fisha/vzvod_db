package com.company.vzvod.entity;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Подписи проверок маршрутов для дашборда:
 * время, строкой ниже — «Фамилия И. О.»
 */
public final class RouteCheckFormatter {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private RouteCheckFormatter() {
    }

    public static String formatEntry(LocalTime checkedAt, String checkerShortFio) {
        String time = checkedAt == null ? "—" : TIME.format(checkedAt);
        String name = checkerShortFio == null ? "" : checkerShortFio.trim();
        if (name.isBlank()) {
            return time;
        }
        return time + "\n" + name;
    }

    public static String joinEntries(List<String> entries) {
        if (entries == null || entries.isEmpty()) {
            return "—";
        }
        return String.join(" — ", entries);
    }
}
