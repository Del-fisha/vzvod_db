package com.company.vzvod.service;

import com.company.vzvod.entity.ServiceBreak;
import com.company.vzvod.entity.ServiceInfo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Чистая выслуга: полные месяцы от начала службы до даты расчёта
 * минус полные календарные месяцы каждого перерыва.
 * Дни, которые не сложились в календарный месяц, копятся по всем перерывам:
 * каждые {@value #DAYS_PER_ACCUMULATED_MONTH} дней дают ещё один месяц.
 * Обе границы перерыва входят в период. Пересечения сливаются, чтобы один день не вычесть дважды.
 */
public final class ServiceBreakRules {

    static final int DAYS_PER_ACCUMULATED_MONTH = 30;

    public static final String DATES_REQUIRED = "Укажите обе даты перерыва";
    public static final String END_BEFORE_START = "Дата «По» раньше даты «С»";
    public static final String BEFORE_SERVICE = "Перерыв раньше начала службы";
    public static final String OVERLAP = "Перерывы стажа пересекаются";
    public static final String NO_START = "Укажите начало службы, чтобы сохранить перерыв";

    private ServiceBreakRules() {
    }

    /**
     * @return текст ошибки или {@code null}, если перерывы можно сохранить
     */
    public static String problem(ServiceInfo serviceInfo) {
        if (serviceInfo == null || serviceInfo.getServiceBreaks() == null || serviceInfo.getServiceBreaks().isEmpty()) {
            return null;
        }
        if (serviceInfo.getStartDate() == null) {
            return NO_START;
        }
        List<Span> spans = new ArrayList<>();
        for (ServiceBreak br : serviceInfo.getServiceBreaks()) {
            if (br == null) {
                continue;
            }
            if (br.getFromDate() == null || br.getToDate() == null) {
                return DATES_REQUIRED;
            }
            if (br.getToDate().isBefore(br.getFromDate())) {
                return END_BEFORE_START;
            }
            if (br.getFromDate().isBefore(serviceInfo.getStartDate())) {
                return BEFORE_SERVICE;
            }
            spans.add(new Span(br.getFromDate(), br.getToDate()));
        }
        spans.sort(Comparator.comparing(Span::from).thenComparing(Span::toInclusive));
        for (int i = 1; i < spans.size(); i++) {
            if (!spans.get(i).from.isAfter(spans.get(i - 1).toInclusive)) {
                return OVERLAP;
            }
        }
        return null;
    }

    /**
     * Сколько полных месяцев вычесть из выслуги на дату {@code onDate}.
     * Учитывается только кусок перерыва внутри {@code [startDate, onDate]}.
     */
    public static int months(List<ServiceBreak> breaks, LocalDate startDate, LocalDate onDate) {
        if (breaks == null || breaks.isEmpty() || startDate == null || onDate == null || onDate.isBefore(startDate)) {
            return 0;
        }
        List<Span> spans = new ArrayList<>();
        for (ServiceBreak br : breaks) {
            if (br == null || br.getFromDate() == null || br.getToDate() == null
                    || br.getToDate().isBefore(br.getFromDate())) {
                continue;
            }
            LocalDate from = br.getFromDate().isBefore(startDate) ? startDate : br.getFromDate();
            LocalDate to = br.getToDate().isAfter(onDate) ? onDate : br.getToDate();
            if (to.isBefore(from)) {
                continue;
            }
            spans.add(new Span(from, to));
        }
        if (spans.isEmpty()) {
            return 0;
        }
        spans.sort(Comparator.comparing(Span::from).thenComparing(Span::toInclusive));
        List<Span> merged = new ArrayList<>();
        for (Span span : spans) {
            if (merged.isEmpty() || span.from.isAfter(merged.get(merged.size() - 1).toInclusive)) {
                merged.add(span);
            } else {
                Span last = merged.get(merged.size() - 1);
                if (span.toInclusive.isAfter(last.toInclusive)) {
                    merged.set(merged.size() - 1, new Span(last.from, span.toInclusive));
                }
            }
        }

        int calendarMonths = 0;
        int leftoverDays = 0;
        for (Span span : merged) {
            LocalDate exclusiveEnd = span.toInclusive.plusDays(1);
            int spanMonths = (int) ChronoUnit.MONTHS.between(span.from, exclusiveEnd);
            int spanDays = (int) ChronoUnit.DAYS.between(span.from.plusMonths(spanMonths), exclusiveEnd);
            calendarMonths += spanMonths;
            leftoverDays += spanDays;
        }
        return calendarMonths + leftoverDays / DAYS_PER_ACCUMULATED_MONTH;
    }

    private record Span(LocalDate from, LocalDate toInclusive) {
    }
}
