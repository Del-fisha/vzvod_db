package com.company.vzvod.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты для сущности AdministrativeViolation")
public class AdministrativeViolationTest extends EntityTestSupport {

    private AdministrativeViolation administrativeViolation;

    @BeforeEach
    void setUp() {
        administrativeViolation = dataManager.create(AdministrativeViolation.class);
    }

    @Test
    @DisplayName("Проверка поля id")
    void testId() {
        assertNotNull(administrativeViolation.getId());

        UUID originalId = administrativeViolation.getId();
        assertEquals(originalId, administrativeViolation.getId());

        UUID newId = UUID.randomUUID();
        administrativeViolation.setId(newId);
        assertEquals(newId, administrativeViolation.getId());
    }

    @Test
    @DisplayName("Проверка поля impact")
    void testImpact() {
        assertNull(administrativeViolation.getImpact());

        administrativeViolation.setImpact(Impact.WITHOUT_IMPACT);
        assertEquals(Impact.WITHOUT_IMPACT, administrativeViolation.getImpact());
    }

    @Test
    @DisplayName("Проверка поля shift")
    void testShift() {
        assertNull(administrativeViolation.getShift());
        Shift shift = dataManager.create(Shift.class);

        administrativeViolation.setShift(shift);
        assertEquals(administrativeViolation.getShift(), shift);
    }

    @Test
    @DisplayName("Проверка поля article")
    void testArticle() {
        assertNull(administrativeViolation.getArticle());

        administrativeViolation.setArticle(ArticleOfAdministrative._11_15);
        assertEquals(ArticleOfAdministrative._11_15, administrativeViolation.getArticle());
    }
}
