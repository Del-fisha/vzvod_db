package com.company.vzvod.service;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import com.company.vzvod.test_support.PreTestEntities;
import io.jmix.core.DataManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

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
    @DisplayName("Проверяет количество дней отпуска в зависимости от стажа: 10, 15, 20+ лет")
    void daysAvailable() {
        serviceInfo.setStartDate(LocalDate.of(2013, 4, 3));
        serviceInfo.getUser().setArmyService(false);

        LocalDate lessThan10Years = LocalDate.of(2023, 4, 2);
        LocalDate moreThan10Years = LocalDate.of(2023, 4, 4);
        LocalDate moreThan15Years = LocalDate.of(2028, 4, 4);
        LocalDate moreThan20Year = LocalDate.of(2033, 4, 4);

        assertEquals(40, VocationService.daysAvailable(serviceInfo, lessThan10Years));
        assertEquals(45, VocationService.daysAvailable(serviceInfo, moreThan10Years));
        assertEquals(50, VocationService.daysAvailable(serviceInfo, moreThan15Years));
        assertEquals(55, VocationService.daysAvailable(serviceInfo, moreThan20Year));

        serviceInfo.getUser().setArmyService(true);

        lessThan10Years = LocalDate.of(2022, 4, 2);
        moreThan10Years = LocalDate.of(2022, 4, 4);
        moreThan15Years = LocalDate.of(2027, 4, 4);
        moreThan20Year = LocalDate.of(2037, 4, 4);

        assertEquals(40, VocationService.daysAvailable(serviceInfo, lessThan10Years));
        assertEquals(45, VocationService.daysAvailable(serviceInfo, moreThan10Years));
        assertEquals(50, VocationService.daysAvailable(serviceInfo, moreThan15Years));
        assertEquals(55, VocationService.daysAvailable(serviceInfo, moreThan20Year));

    }
}