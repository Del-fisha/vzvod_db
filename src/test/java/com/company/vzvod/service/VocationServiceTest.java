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
    @DisplayName("Норматив дней только по порогам 10 и 15 лет (без 20‑летней надстройки)")
    void nominalDays_availableByTenureThresholds() {
        serviceInfo.setStartDate(LocalDate.of(2013, 4, 3));
        serviceInfo.getUser().setArmyService(ArmyService.NOT_SERVED);

        LocalDate lessThan10Years = LocalDate.of(2023, 4, 2);
        LocalDate tenYearsInclusive = LocalDate.of(2023, 4, 3);
        LocalDate fifteenYearsInclusive = LocalDate.of(2028, 4, 3);

        assertEquals(40, VocationService.nominalDaysAvailable(serviceInfo, lessThan10Years));
        assertEquals(45, VocationService.nominalDaysAvailable(serviceInfo, tenYearsInclusive));
        assertEquals(50, VocationService.nominalDaysAvailable(serviceInfo, fifteenYearsInclusive));

        LocalDate twentyYears = LocalDate.of(2033, 4, 3);
        assertEquals(50, VocationService.nominalDaysAvailable(serviceInfo, twentyYears));

        serviceInfo.getUser().setArmyService(ArmyService.SERVED);

        assertEquals(40, VocationService.nominalDaysAvailable(serviceInfo, LocalDate.of(2022, 4, 2)));
        assertEquals(45, VocationService.nominalDaysAvailable(serviceInfo, LocalDate.of(2022, 4, 3)));
        assertEquals(50, VocationService.nominalDaysAvailable(serviceInfo, LocalDate.of(2027, 4, 3)));
        assertEquals(50, VocationService.nominalDaysAvailable(serviceInfo, LocalDate.of(2037, 4, 3)));
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
    void poolDaysDebitedSubtractsAddedDaysOnly() {
        assertEquals(8, VocationService.poolDaysDebited(10, 2));
        assertEquals(10, VocationService.poolDaysDebited(10, null));
        assertEquals(0, VocationService.poolDaysDebited(2, 5));
    }
}
