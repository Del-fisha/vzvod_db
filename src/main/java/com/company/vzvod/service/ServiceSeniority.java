package com.company.vzvod.service;

import com.company.vzvod.entity.ServiceBreak;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Чистая выслуга для карточки сотрудника: календарный период от начала службы
 * до даты расчёта минус дни прерываний стажа.
 * <p>
 * Дата расчёта в период не входит как полный день — так же, как {@link Period#between}.
 * Обе границы прерывания входят в вычитаемый интервал, но только дни строго до даты расчёта.
 * Пересечения сливаются, чтобы один день не вычесть дважды.
 * Год службы в армии сюда не добавляется: это стаж по дате начала службы.
 */
public final class ServiceSeniority {

    private ServiceSeniority() {
    }

    public static Period net(LocalDate startDate, LocalDate onDate, List<ServiceBreak> breaks) {
        if (startDate == null || onDate == null || onDate.isBefore(startDate)) {
            return Period.ZERO;
        }
        long breakDays = breakDaysBefore(breaks, startDate, onDate);
        LocalDate virtualEnd = onDate.minusDays(breakDays);
        if (virtualEnd.isBefore(startDate)) {
            return Period.ZERO;
        }
        return Period.between(startDate, virtualEnd);
    }

    /**
     * «6 лет, 10 месяцев, 18 дней» — с падежом по числу.
     */
    public static String format(Period period) {
        Period value = period == null ? Period.ZERO : period;
        return value.getYears() + " " + word(value.getYears(), "год", "года", "лет")
                + ", " + value.getMonths() + " " + word(value.getMonths(), "месяц", "месяца", "месяцев")
                + ", " + value.getDays() + " " + word(value.getDays(), "день", "дня", "дней");
    }

    /** Годы и месяцы той же выслуги, без дней: «6 лет, 10 месяцев». */
    public static String formatYearsMonths(Period period) {
        Period value = period == null ? Period.ZERO : period;
        return value.getYears() + " " + word(value.getYears(), "год", "года", "лет")
                + ", " + value.getMonths() + " " + word(value.getMonths(), "месяц", "месяца", "месяцев");
    }

    /**
     * Дни прерываний внутри {@code [startDate, onDate)}.
     */
    static long breakDaysBefore(List<ServiceBreak> breaks, LocalDate startDate, LocalDate onDate) {
        if (breaks == null || breaks.isEmpty() || startDate == null || onDate == null || !onDate.isAfter(startDate)) {
            return 0;
        }
        List<Span> spans = new ArrayList<>();
        for (ServiceBreak br : breaks) {
            if (br == null || br.getFromDate() == null || br.getToDate() == null
                    || br.getToDate().isBefore(br.getFromDate())) {
                continue;
            }
            LocalDate from = br.getFromDate().isBefore(startDate) ? startDate : br.getFromDate();
            LocalDate toExclusive = br.getToDate().plusDays(1);
            if (toExclusive.isAfter(onDate)) {
                toExclusive = onDate;
            }
            if (!toExclusive.isAfter(from)) {
                continue;
            }
            spans.add(new Span(from, toExclusive));
        }
        if (spans.isEmpty()) {
            return 0;
        }
        spans.sort(Comparator.comparing(Span::from).thenComparing(Span::toExclusive));
        List<Span> merged = new ArrayList<>();
        for (Span span : spans) {
            if (merged.isEmpty() || !span.from.isBefore(merged.get(merged.size() - 1).toExclusive)) {
                merged.add(span);
            } else {
                Span last = merged.get(merged.size() - 1);
                if (span.toExclusive.isAfter(last.toExclusive)) {
                    merged.set(merged.size() - 1, new Span(last.from, span.toExclusive));
                }
            }
        }
        long days = 0;
        for (Span span : merged) {
            days += ChronoUnit.DAYS.between(span.from, span.toExclusive);
        }
        return days;
    }

    private static String word(int value, String one, String few, String many) {
        int abs = Math.abs(value) % 100;
        int last = abs % 10;
        if (abs > 10 && abs < 20) {
            return many;
        }
        if (last == 1) {
            return one;
        }
        if (last > 1 && last < 5) {
            return few;
        }
        return many;
    }

    private record Span(LocalDate from, LocalDate toExclusive) {
    }
}
