package com.company.vzvod.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit-тесты для IdCard Entity")
public class IdCardTest extends EntityTestSupport {

    private IdCard idCard;

    @BeforeEach
    void setUp() {
        idCard = dataManager.create(IdCard.class);
    }

    @Test
    @DisplayName("Проверка установки и получения ID")
    void testId() {
        UUID originalId = idCard.getId();
        assertNotNull(originalId);

        UUID newId = UUID.randomUUID();
        idCard.setId(newId);
        assertEquals(newId, idCard.getId());
    }

    @Test
    @DisplayName("Проверка поля spl")
    void testSpl() {
        assertNull(idCard.getSpl());

        String spl = "123456";
        idCard.setSpl(spl);

        assertEquals(spl, idCard.getSpl());
    }

    @Test
    @DisplayName("Проверка поля issued")
    void testIssued() {
        assertNull(idCard.getIssued());

        LocalDate date = LocalDate.now().minusDays(10);
        idCard.setIssued(date);
        assertEquals(date, idCard.getIssued());
    }

    @Test
    @DisplayName("Проверка поля until")
    void testUntil() {
        assertNull(idCard.getUntil());

        LocalDate date = LocalDate.now().plusDays(10);
        idCard.setUntil(date);
        assertEquals(date, idCard.getUntil());
    }
}
