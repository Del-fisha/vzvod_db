package com.company.vzvod.service;

import com.company.vzvod.entity.ArmyService;
import com.company.vzvod.entity.ServiceBreak;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import com.company.vzvod.entity.VocationType;
import com.company.vzvod.test_support.PreTestEntities;
import io.jmix.core.DataManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VocationServiceTest {

    @Autowired
    protected DataManager dataManager;

    @Autowired
    ServiceInfo serviceInfo;

    @Autowired
    User user;

    @BeforeEach
    void setUp() {
        serviceInfo = new ServiceInfo();
        user = new User();
        PreTestEntities.updateUser(user);
        PreTestEntities.updateServiceInfo(serviceInfo);

        serviceInfo.setUser(user);
        user.setServiceInfo(serviceInfo);
    }

    @Test
    @DisplayName("Норматив дней по порогам 10, 15 и 20 лет")
    void nominalDays_availableByTenureThresholds() {
        serviceInfo.setStartDate(LocalDate.of(2013, 4, 3));
        serviceInfo.getUser().setArmyService(ArmyService.NOT_SERVED);

        LocalDate lessThan10Years = LocalDate.of(2023, 4, 2);
        LocalDate tenYearsInclusive = LocalDate.of(2023, 4, 3);
        LocalDate fifteenYearsInclusive = LocalDate.of(2028, 4, 3);
        LocalDate lessThan20Years = LocalDate.of(2033, 4, 2);
        LocalDate twentyYearsInclusive = LocalDate.of(2033, 4, 3);

        assertEquals(40, VocationService.nominalDaysAvailable(serviceInfo, lessThan10Years));
        assertEquals(45, VocationService.nominalDaysAvailable(serviceInfo, tenYearsInclusive));
        assertEquals(50, VocationService.nominalDaysAvailable(serviceInfo, fifteenYearsInclusive));
        assertEquals(50, VocationService.nominalDaysAvailable(serviceInfo, lessThan20Years));
        assertEquals(55, VocationService.nominalDaysAvailable(serviceInfo, twentyYearsInclusive));

        serviceInfo.getUser().setArmyService(ArmyService.SERVED);

        assertEquals(40, VocationService.nominalDaysAvailable(serviceInfo, LocalDate.of(2022, 4, 2)));
        assertEquals(45, VocationService.nominalDaysAvailable(serviceInfo, LocalDate.of(2022, 4, 3)));
        assertEquals(50, VocationService.nominalDaysAvailable(serviceInfo, LocalDate.of(2027, 4, 3)));
        assertEquals(55, VocationService.nominalDaysAvailable(serviceInfo, LocalDate.of(2032, 4, 3)));
        assertEquals(55, VocationService.nominalDaysAvailable(serviceInfo, LocalDate.of(2037, 4, 3)));
    }

    @Test
    @DisplayName("Общая выслуга в месяцах и отображение полными годами")
    void effectiveMonths_andYears() {
        serviceInfo.setStartDate(LocalDate.of(2016, 1, 1));
        serviceInfo.getUser().setArmyService(ArmyService.NOT_SERVED);
        LocalDate onDate = LocalDate.of(2025, 6, 1);

        assertEquals(113, VocationService.effectiveMonths(serviceInfo, onDate));
        assertEquals(9, VocationService.effectiveYears(serviceInfo, onDate));
    }

    @Test
    @DisplayName("+5 дней после 20 лет внутри года, если порог не пройден на 01.01")
    void midYear_bonusAfterCrossingTwenty() {
        serviceInfo.setStartDate(LocalDate.of(2003, 11, 7));
        serviceInfo.getUser().setArmyService(ArmyService.NOT_SERVED);

        LocalDate y2023 = LocalDate.of(2023, 1, 1);
        assertEquals(0,
                VocationService.midYearSeniorityBonuses(serviceInfo, y2023, LocalDate.of(2023, 11, 6)));
        assertEquals(5,
                VocationService.midYearSeniorityBonuses(serviceInfo, y2023, LocalDate.of(2023, 11, 15)));

        LocalDate y2024 = LocalDate.of(2024, 1, 1);
        assertEquals(0,
                VocationService.midYearSeniorityBonuses(serviceInfo, y2024, LocalDate.of(2024, 12, 31)));
    }

    @Test
    @DisplayName("+5 дней после 10 и 15 лет внутри года, если порог не пройден на 01.01")
    void midYear_bonusAfterCrossingTenAndFifteen() {
        serviceInfo.setStartDate(LocalDate.of(2013, 4, 4));
        serviceInfo.getUser().setArmyService(ArmyService.SERVED);

        LocalDate y2022 = LocalDate.of(2022, 1, 1);
        assertEquals(0,
                VocationService.midYearSeniorityBonuses(serviceInfo, y2022, LocalDate.of(2022, 4, 3)));
        assertEquals(5,
                VocationService.midYearSeniorityBonuses(serviceInfo, y2022, LocalDate.of(2022, 4, 15)));

        // С 01.01.2023 номинальный лимит уже 45 без бонуса за выход на 10 лет
        LocalDate y2023 = LocalDate.of(2023, 1, 1);
        assertEquals(0,
                VocationService.midYearSeniorityBonuses(serviceInfo, y2023, LocalDate.of(2023, 12, 31)));
    }

    @Test
    @DisplayName("Три перерыва по 20 дней уменьшают выслугу на 2 месяца")
    void threeShortBreaks_subtractTwoMonths() {
        serviceInfo.setStartDate(LocalDate.of(2014, 1, 10));
        serviceInfo.getUser().setArmyService(ArmyService.NOT_SERVED);
        LocalDate onDate = LocalDate.of(2024, 1, 10);

        assertEquals(120, VocationService.effectiveMonths(serviceInfo, onDate));
        assertEquals(45, VocationService.nominalDaysAvailable(serviceInfo, onDate));

        serviceInfo.setServiceBreaks(List.of(
                breakBetween(LocalDate.of(2016, 2, 1), LocalDate.of(2016, 2, 20)),
                breakBetween(LocalDate.of(2018, 5, 1), LocalDate.of(2018, 5, 20)),
                breakBetween(LocalDate.of(2021, 9, 1), LocalDate.of(2021, 9, 20))
        ));

        assertEquals(118, VocationService.effectiveMonths(serviceInfo, onDate));
        assertEquals(40, VocationService.nominalDaysAvailable(serviceInfo, onDate));
    }

    @Test
    @DisplayName("Один перерыв короче 30 дней месяц не отнимает, ровно 30 дней отнимают месяц")
    void leftoverDays_accumulateToThirty() {
        serviceInfo.setStartDate(LocalDate.of(2014, 1, 10));
        serviceInfo.getUser().setArmyService(ArmyService.NOT_SERVED);
        LocalDate onDate = LocalDate.of(2024, 1, 10);

        serviceInfo.setServiceBreaks(List.of(
                breakBetween(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 1, 20))
        ));
        assertEquals(120, VocationService.effectiveMonths(serviceInfo, onDate));

        serviceInfo.setServiceBreaks(List.of(
                breakBetween(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 1, 29))
        ));
        assertEquals(120, VocationService.effectiveMonths(serviceInfo, onDate));

        serviceInfo.setServiceBreaks(List.of(
                breakBetween(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 1, 30))
        ));
        assertEquals(119, VocationService.effectiveMonths(serviceInfo, onDate));
    }

    @Test
    @DisplayName("Календарный месяц перерыва и хвосты дней с разных перерывов складываются")
    void calendarMonthPlusLeftoverDays() {
        serviceInfo.setStartDate(LocalDate.of(2014, 1, 10));
        serviceInfo.getUser().setArmyService(ArmyService.NOT_SERVED);
        LocalDate onDate = LocalDate.of(2024, 1, 10);
        serviceInfo.setServiceBreaks(List.of(
                breakBetween(LocalDate.of(2018, 1, 1), LocalDate.of(2018, 2, 20)),
                breakBetween(LocalDate.of(2019, 4, 1), LocalDate.of(2019, 4, 10))
        ));

        assertEquals(118, VocationService.effectiveMonths(serviceInfo, onDate));
        assertEquals(40, VocationService.nominalDaysAvailable(serviceInfo, onDate));
    }

    @Test
    @DisplayName("Пересечение вычитается один раз, кусок вне службы не считается")
    void overlapMergedAndOutsideWindowIgnored() {
        serviceInfo.setStartDate(LocalDate.of(2016, 6, 1));
        serviceInfo.getUser().setArmyService(ArmyService.NOT_SERVED);
        LocalDate onDate = LocalDate.of(2026, 6, 1);

        serviceInfo.setServiceBreaks(List.of(
                breakBetween(LocalDate.of(2015, 1, 1), LocalDate.of(2015, 6, 1))
        ));
        assertEquals(120, VocationService.effectiveMonths(serviceInfo, onDate));

        serviceInfo.setServiceBreaks(List.of(
                breakBetween(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 6, 30))
        ));
        assertEquals(119, VocationService.effectiveMonths(serviceInfo, onDate));

        serviceInfo.setServiceBreaks(List.of(
                breakBetween(LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 20)),
                breakBetween(LocalDate.of(2018, 1, 10), LocalDate.of(2018, 1, 25))
        ));
        assertEquals(120, VocationService.effectiveMonths(serviceInfo, onDate));

        serviceInfo.setServiceBreaks(List.of(
                breakBetween(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 8, 1))
        ));
        assertEquals(119, VocationService.effectiveMonths(serviceInfo, onDate));
    }

    @Test
    @DisplayName("Год за армию прибавляется после вычитания перерыва")
    void armyCredit_appliedAfterBreaks() {
        serviceInfo.setStartDate(LocalDate.of(2015, 1, 1));
        serviceInfo.getUser().setArmyService(ArmyService.SERVED);
        LocalDate onDate = LocalDate.of(2024, 1, 1);

        assertEquals(120, VocationService.effectiveMonths(serviceInfo, onDate));
        assertEquals(45, VocationService.nominalDaysAvailable(serviceInfo, onDate));

        serviceInfo.setServiceBreaks(List.of(
                breakBetween(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 30))
        ));
        assertEquals(119, VocationService.effectiveMonths(serviceInfo, onDate));
        assertEquals(40, VocationService.nominalDaysAvailable(serviceInfo, onDate));
    }

    @Test
    @DisplayName("Перерыв сдвигает день, когда внутри года наступает порог 10 лет")
    void breakShiftsMidYearThreshold() {
        serviceInfo.setStartDate(LocalDate.of(2013, 4, 3));
        serviceInfo.getUser().setArmyService(ArmyService.NOT_SERVED);
        serviceInfo.setServiceBreaks(List.of(
                breakBetween(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 30))
        ));

        LocalDate yearStart = LocalDate.of(2023, 1, 1);
        assertEquals(0, VocationService.midYearSeniorityBonuses(serviceInfo, yearStart, LocalDate.of(2023, 5, 2)));
        assertEquals(5, VocationService.midYearSeniorityBonuses(serviceInfo, yearStart, LocalDate.of(2023, 5, 3)));
    }

    private static ServiceBreak breakBetween(LocalDate from, LocalDate to) {
        ServiceBreak serviceBreak = new ServiceBreak();
        serviceBreak.setFromDate(from);
        serviceBreak.setToDate(to);
        return serviceBreak;
    }

    @Test
    @DisplayName("Дни из «добавлено дней» не входят в countOfDays и из котла не вычитаются")
    void poolDaysDebited_countsCalendarDaysOnly() {
        assertEquals(11, VocationService.poolDaysDebited(11, 3));
        assertEquals(10, VocationService.poolDaysDebited(10, null));
        assertEquals(2, VocationService.poolDaysDebited(2, 5));
    }

    @Test
    @DisplayName("Показанный конец отпуска сдвигается на добавленные дни")
    void absenceEnd_extendsStoredEndByAddedDays() {
        LocalDate end = LocalDate.of(2026, 2, 20);
        assertEquals(LocalDate.of(2026, 2, 23), VocationService.absenceEnd(end, 3));
        assertEquals(end, VocationService.absenceEnd(end, 0));
        assertEquals(end, VocationService.absenceEnd(end, null));
        assertEquals(end, VocationService.absenceEnd(end, -2));
    }

    @Test
    @DisplayName("Отсутствие включает добавленные дни после сохранённого конца")
    void coversDay_includesAddedDaysAfterEnd() {
        LocalDate start = LocalDate.of(2026, 2, 10);
        LocalDate end = LocalDate.of(2026, 2, 20);

        assertTrue(VocationService.coversDay(start, end, 3, LocalDate.of(2026, 2, 10)));
        assertTrue(VocationService.coversDay(start, end, 3, LocalDate.of(2026, 2, 20)));
        assertTrue(VocationService.coversDay(start, end, 3, LocalDate.of(2026, 2, 23)));
        assertFalse(VocationService.coversDay(start, end, 3, LocalDate.of(2026, 2, 9)));
        assertFalse(VocationService.coversDay(start, end, 3, LocalDate.of(2026, 2, 24)));
        assertFalse(VocationService.coversDay(start, end, 0, LocalDate.of(2026, 2, 21)));
    }

    @Test
    @DisplayName("Из котла списываются только календарные дни основного, дополнительного и части основного")
    void daysDebitedFromPool_ignoresStudyPtcAndAddedDays() {
        assertEquals(11, VocationService.daysDebitedFromPool(VocationType.MAIN, 11));
        assertEquals(5, VocationService.daysDebitedFromPool(VocationType.ADDITIONAL, 5));
        assertEquals(4, VocationService.daysDebitedFromPool(VocationType.PART_OF_MAIN, 4));
        assertEquals(0, VocationService.daysDebitedFromPool(VocationType.STUDY_LEAVE, 14));
        assertEquals(0, VocationService.daysDebitedFromPool(VocationType.PTC, 10));
        assertEquals(0, VocationService.daysDebitedFromPool(null, 10));
    }
}
