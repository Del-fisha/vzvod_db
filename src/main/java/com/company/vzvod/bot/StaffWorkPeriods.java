package com.company.vzvod.bot;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

/**
 * Границы «за месяц» и «за этот год»: счётчики начинаются заново 1-го числа и 1 января.
 */
public final class StaffWorkPeriods {

    public static final ZoneId MOSCOW = ZoneId.of("Europe/Moscow");

    public record Range(LocalDate from, LocalDate to) {
    }

    private StaffWorkPeriods() {
    }

    public static Range month(LocalDate today) {
        YearMonth month = YearMonth.from(today);
        return new Range(month.atDay(1), month.atEndOfMonth());
    }

    public static Range year(LocalDate today) {
        int y = today.getYear();
        return new Range(LocalDate.of(y, 1, 1), LocalDate.of(y, 12, 31));
    }
}
