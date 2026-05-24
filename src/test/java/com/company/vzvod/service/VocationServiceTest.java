package com.company.vzvod.service;

import com.company.vzvod.entity.ArmyService;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import com.company.vzvod.test_support.PreTestEntities;
import io.jmix.core.DataManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        serviceInfo.setMonthsOfServiceBeforeLastAppointment(12);
        serviceInfo.getUser().setArmyService(ArmyService.NOT_SERVED);
        LocalDate onDate = LocalDate.of(2025, 6, 1);

        assertEquals(125, VocationService.effectiveMonths(serviceInfo, onDate));
        assertEquals(10, VocationService.effectiveYears(serviceInfo, onDate));
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
    @DisplayName("Месяцы выслуги до последнего устройства увеличивают стаж для норматива отпуска")
    void priorServiceMonths_increaseEffectiveTenure() {
        serviceInfo.setStartDate(LocalDate.of(2016, 1, 1));
        serviceInfo.getUser().setArmyService(ArmyService.NOT_SERVED);
        LocalDate onDate = LocalDate.of(2025, 6, 1);

        serviceInfo.setMonthsOfServiceBeforeLastAppointment(12);
        assertEquals(45, VocationService.nominalDaysAvailable(serviceInfo, onDate));

        serviceInfo.setMonthsOfServiceBeforeLastAppointment(0);
        assertEquals(40, VocationService.nominalDaysAvailable(serviceInfo, onDate));
    }

    @Test
    void poolDaysDebitedSubtractsAddedDaysOnly() {
        assertEquals(8, VocationService.poolDaysDebited(10, 2));
        assertEquals(10, VocationService.poolDaysDebited(10, null));
        assertEquals(0, VocationService.poolDaysDebited(2, 5));
    }
}
