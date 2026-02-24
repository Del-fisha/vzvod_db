package com.company.vzvod.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit-тесты для Incentive Entity")
class IncentiveTest extends EntityTestSupport {

    Incentive incentive;

    @BeforeEach
    void setUp() {
        incentive = dataManager.create(Incentive.class);
    }

    @Test
    @DisplayName("Проверка поля description")
    void testDescription() {
        assertNull(incentive.getDescription());

        String description = "Test text of description";
        incentive.setDescription(description);
        assertEquals(description, incentive.getDescription());
    }


    @Test
    @DisplayName("Проверка поля orderNumber")
    void testOrderNumber() {
        assertNull(incentive.getOrderNumber());

        String orderNumber = "123456789";
        incentive.setOrderNumber(orderNumber);
        assertEquals(orderNumber, incentive.getOrderNumber());
    }


    @Test
    @DisplayName("Проверка поля date")
    void testDate() {
        assertNull(incentive.getDate());

        LocalDate localDate = LocalDate.now();
        incentive.setDate(localDate);
        assertEquals(localDate, incentive.getDate());
    }


    @Test
    @DisplayName("Проверка поля type")
    void testIncentiveType() {
        assertNull(incentive.getIncentiveType());

        incentive.setIncentiveType(IncentiveType.MEDAL);
        assertSame(IncentiveType.MEDAL, incentive.getIncentiveType());
    }

    @Test
    @DisplayName("Проверка поля initiator")
    void testInitiator() {
        assertNull(incentive.getInitiator());

        incentive.setInitiator(Initiator.GU);
        assertSame(Initiator.GU, incentive.getInitiator());
    }

    @Test
    @DisplayName("Проверка поля serviceInfo")
    void testUserServiceInfo() {
        assertNull(incentive.getUserServiceInfo());

        ServiceInfo serviceInfo = dataManager.create(ServiceInfo.class);
        incentive.setUserServiceInfo(serviceInfo);
        assertSame(serviceInfo, incentive.getUserServiceInfo());
    }


    @Test
    @DisplayName("Проверка поля Id")
    void testId() {
        UUID originalId = incentive.getId();
        assertNotNull(originalId);

        UUID newId = UUID.randomUUID();
        incentive.setId(newId);

        assertSame(newId, incentive.getId());
    }
}