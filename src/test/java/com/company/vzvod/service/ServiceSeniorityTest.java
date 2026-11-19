package com.company.vzvod.service;

import com.company.vzvod.entity.ServiceBreak;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceSeniorityTest {

    @Test
    @DisplayName("Без перерывов — календарный период от начала службы до даты расчёта")
    void noBreaks_calendarPeriod() {
        LocalDate start = LocalDate.of(2016, 1, 1);
        LocalDate onDate = LocalDate.of(2026, 11, 19);

        Period net = ServiceSeniority.net(start, onDate, List.of());

        assertEquals(Period.of(10, 10, 18), net);
        assertEquals("10 лет, 10 месяцев, 18 дней", ServiceSeniority.format(net));
    }

    @Test
    @DisplayName("Ровно N лет в годовщину")
    void anniversary_wholeYears() {
        assertEquals(Period.of(10, 0, 0), ServiceSeniority.net(
                LocalDate.of(2014, 1, 10),
                LocalDate.of(2024, 1, 10),
                List.of()));
        assertEquals("10 лет, 0 месяцев, 0 дней", ServiceSeniority.format(Period.of(10, 0, 0)));
    }

    @Test
    @DisplayName("Дни прерываний вычитаются из конца периода, пересечение — один раз")
    void breaksSubtractedAndOverlapsMerged() {
        LocalDate start = LocalDate.of(2014, 1, 10);
        LocalDate onDate = LocalDate.of(2024, 1, 10);

        Period threeTimesTwentyDays = ServiceSeniority.net(start, onDate, List.of(
                breakBetween(LocalDate.of(2016, 2, 1), LocalDate.of(2016, 2, 20)),
                breakBetween(LocalDate.of(2018, 5, 1), LocalDate.of(2018, 5, 20)),
                breakBetween(LocalDate.of(2021, 9, 1), LocalDate.of(2021, 9, 20))
        ));
        assertEquals(Period.between(start, onDate.minusDays(60)), threeTimesTwentyDays);
        assertEquals("9 лет, 10 месяцев, 1 день", ServiceSeniority.format(threeTimesTwentyDays));

        Period overlapped = ServiceSeniority.net(start, onDate, List.of(
                breakBetween(LocalDate.of(2016, 2, 1), LocalDate.of(2016, 2, 20)),
                breakBetween(LocalDate.of(2016, 2, 10), LocalDate.of(2016, 2, 25))
        ));
        assertEquals(Period.between(start, onDate.minusDays(25)), overlapped);
    }

    @Test
    @DisplayName("Прерывание в дату расчёта ещё не уменьшает выслугу, будущее и дырявые даты пропускаются")
    void breakOnCalculationDateAndInvalidRowsIgnored() {
        LocalDate start = LocalDate.of(2020, 1, 1);
        LocalDate onDate = LocalDate.of(2026, 1, 1);

        assertEquals(Period.of(6, 0, 0), ServiceSeniority.net(start, onDate, List.of(
                breakBetween(onDate, onDate),
                breakBetween(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 1)),
                breakBetween(null, onDate),
                breakBetween(LocalDate.of(2021, 5, 2), LocalDate.of(2021, 5, 1))
        )));
    }

    @Test
    @DisplayName("Прерывание длиннее службы даёт ноль; нет даты начала — ноль")
    void longerThanServiceOrMissingStart() {
        assertEquals(Period.ZERO, ServiceSeniority.net(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1),
                List.of(breakBetween(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)))));
        assertEquals(Period.ZERO, ServiceSeniority.net(null, LocalDate.of(2024, 2, 1), List.of()));
        assertEquals("0 лет, 0 месяцев, 0 дней", ServiceSeniority.format(Period.ZERO));
        assertEquals("1 год, 1 месяц, 1 день", ServiceSeniority.format(Period.of(1, 1, 1)));
        assertEquals("2 года, 2 месяца, 2 дня", ServiceSeniority.format(Period.of(2, 2, 2)));
        assertEquals("11 лет, 11 месяцев, 11 дней", ServiceSeniority.format(Period.of(11, 11, 11)));
        assertEquals("21 год, 21 месяц, 21 день", ServiceSeniority.format(Period.of(21, 21, 21)));
    }

    private static ServiceBreak breakBetween(LocalDate from, LocalDate to) {
        ServiceBreak serviceBreak = new ServiceBreak();
        serviceBreak.setFromDate(from);
        serviceBreak.setToDate(to);
        return serviceBreak;
    }
}
