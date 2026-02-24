package com.company.vzvod.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VocationTest extends EntityTestSupport {

    Vocation vocation;

    @BeforeEach
    void setUp() {
        vocation = dataManager.create(Vocation.class);
    }

    @Test
    void testUserServiceInfo() {
        assertNull(vocation.getUserServiceInfo());

        ServiceInfo serviceInfo = dataManager.create(ServiceInfo.class);

        vocation.setUserServiceInfo(serviceInfo);
        assertSame(serviceInfo, vocation.getUserServiceInfo());
    }

    @Test
    void testType() {
        assertNull(vocation.getType());

        vocation.setType(VocationType.MAIN);
        assertSame(VocationType.MAIN, vocation.getType());
    }

    @Test
    void testStartDate() {
        assertNull(vocation.getStartDate());

        LocalDate date = LocalDate.now();
        vocation.setStartDate(date);

        assertSame(date, vocation.getStartDate());
    }

    @Test
    void testEndDate() {
        assertNull(vocation.getEndDate());

        LocalDate date = LocalDate.now();
        vocation.setEndDate(date);

        assertSame(date, vocation.getEndDate());
    }

    @Test
    void testCountOfDays() {
        assertNull(vocation.getCountOfDays());

        int countOfDays = 40;
        vocation.setCountOfDays(countOfDays);

        assertEquals(countOfDays, (int) vocation.getCountOfDays());
    }

    @Test
    void testAllRemaindDays() {
        assertNull(vocation.getAllRemaindDays());

        int allRemainedDays = 10;
        vocation.setAllRemaindDays(10);

        assertEquals(allRemainedDays, vocation.getAllRemaindDays());
    }

    @Test
    void testHasDeparture() {
        assertNull(vocation.isHasDeparture());

        Boolean hasDeparture = true;
        vocation.setHasDeparture(hasDeparture);

        assertEquals(hasDeparture, vocation.isHasDeparture());
    }

    @Test
    void testCityToDrive() {
        assertNull(vocation.getCityToDrive());

        String city = "Москва";
        vocation.setCityToDrive(city);
        assertEquals(city, vocation.getCityToDrive());
    }

    @Test
    void testDaysAddedByDeparture() {
        assertNull(vocation.getDaysAddedByDeparture());

        Integer addedDays = 2;
        vocation.setDaysAddedByDeparture(addedDays);

        assertEquals(addedDays, vocation.getDaysAddedByDeparture());
    }

    @Test
    void testId() {
        UUID originalId = vocation.getId();
        assertNotNull(originalId);

        UUID newId = UUID.randomUUID();
        vocation.setId(newId);
        assertEquals(newId, vocation.getId());
    }
}