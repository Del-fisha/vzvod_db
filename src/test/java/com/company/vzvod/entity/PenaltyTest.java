package com.company.vzvod.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit-тесты для Penalty Entity")
public class PenaltyTest extends EntityTestSupport {

    Penalty penalty;

    @BeforeEach
    void setUp() {
        penalty = dataManager.create(Penalty.class);
    }

    @Test
    @DisplayName("Проверка поля Id")
    void testId() {
        UUID originalId = penalty.getId();
        assertNotNull(originalId);

        UUID newId = UUID.randomUUID();
        penalty.setId(newId);

        assertSame(newId, penalty.getId());
    }

    @Test
    @DisplayName("Проверка поля serviceInfo")
    void testUserServiceInfo() {
        assertNull(penalty.getUserServiceInfo());

        ServiceInfo serviceInfo = dataManager.create(ServiceInfo.class);
        penalty.setUserServiceInfo(serviceInfo);
        assertSame(serviceInfo, penalty.getUserServiceInfo());
    }

    @Test
    @DisplayName("Проверка поля initiator")
    void testInitiator() {
        assertNull(penalty.getInitiator());

        penalty.setInitiator(Initiator.GU);
        assertSame(Initiator.GU, penalty.getInitiator());
    }

    @Test
    @DisplayName("Проверка поля type")
    void testIncentiveType() {
        assertNull(penalty.getPenaltyType());

        penalty.setPenaltyType(PenaltyType.DEMOTION);
        assertSame(PenaltyType.DEMOTION, penalty.getPenaltyType());
    }

    @Test
    @DisplayName("Проверка поля date")
    void testDate() {
        assertNull(penalty.getDate());

        LocalDate localDate = LocalDate.now();
        penalty.setDate(localDate);
        assertEquals(localDate, penalty.getDate());
    }

    @Test
    @DisplayName("Проверка поля orderNumber")
    void testOrderNumber() {
        assertNull(penalty.getOrderNumber());

        String orderNumber = "123456789";
        penalty.setOrderNumber(orderNumber);
        assertEquals(orderNumber, penalty.getOrderNumber());
    }

    @Test
    @DisplayName("Проверка поля description")
    void testDescription() {
        assertNull(penalty.getDescription());

        String description = "Test text of description";
        penalty.setDescription(description);
        assertEquals(description, penalty.getDescription());
    }

    @Test
    @DisplayName("Проверка поля status")
    void testStatus() {
        assertNull(penalty.getPenaltyStatus());

        penalty.setPenaltyStatus(PenaltyStatus.ACTIVE);
        assertSame(PenaltyStatus.ACTIVE, penalty.getPenaltyStatus());
    }
}
