package com.company.vzvod.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("Тесты для сущности CriminalViolation")
public class CriminalViolationTest extends EntityTestSupport {

    private CriminalViolation criminalViolation;

    @BeforeEach
    void setUp() {
        criminalViolation = dataManager.create(CriminalViolation.class);
    }

    @Test
    @DisplayName("Проверка поля id")
    void testId() {
        assertNotNull(criminalViolation.getId());

        UUID originalId = criminalViolation.getId();
        assertEquals(originalId, criminalViolation.getId());

        UUID newId = UUID.randomUUID();
        criminalViolation.setId(newId);
        assertEquals(newId, criminalViolation.getId());
    }

    @Test
    @DisplayName("Проверка поля impact")
    void testImpact() {
        assertNull(criminalViolation.getImpact());

        criminalViolation.setImpact(Impact.WITHOUT_IMPACT);
        assertEquals(Impact.WITHOUT_IMPACT, criminalViolation.getImpact());
    }

    @Test
    @DisplayName("Проверка поля shift")
    void testShift() {
        assertNull(criminalViolation.getShift());
        Shift shift = dataManager.create(Shift.class);

        criminalViolation.setShift(shift);
        assertEquals(criminalViolation.getShift(), shift);
    }

    @Test
    @DisplayName("Проверка поля type")
    void testArticle() {
        assertNull(criminalViolation.getType());

        criminalViolation.setType(TypeOfCriminal.FEDERAL_WANTED);
        assertEquals(TypeOfCriminal.FEDERAL_WANTED, criminalViolation.getType());
    }
}
