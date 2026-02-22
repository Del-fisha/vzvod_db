package com.company.vzvod.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit-тесты для Education Entity")
public class EducationTest extends EntityTestSupport {

    private Education education;

    @BeforeEach
    void setUp() {
        education = dataManager.create(Education.class);
    }

    @Test
    @DisplayName("Проверка установки и получения ID")
    void testId() {
        UUID originalId = education.getId();
        assertNotNull(originalId, "ID не должен быть null после создания через DataManager");
        UUID newId = UUID.randomUUID();
        education.setId(newId);
        assertEquals(newId, education.getId(), "ID должен измениться после setter");
    }

    @Test
    @DisplayName("Установка и получение дат")
    void testDates() {
        LocalDate started = LocalDate.now().minusYears(10);
        LocalDate until = LocalDate.now().minusYears(5);

        assertNull(education.getStarted());
        assertNull(education.getUntil());

        education.setStarted(started);
        education.setUntil(until);

        assertEquals(started, education.getStarted());
        assertEquals(until, education.getUntil());
    }

    @Test
    @DisplayName("Установка и получение type (enum)")
    void testType() {

        assertNull(education.getType());

        education.setType(TypeOfEducation.UNIVERSITY);
        assertEquals(TypeOfEducation.UNIVERSITY, education.getType());
    }

    @Test
    @DisplayName("Установка и получение status (enum)")
    void testStatus() {

        assertNull(education.getStatus());

        education.setStatus(EducationStatus.AT_THE_MOMENT);
        assertEquals(EducationStatus.AT_THE_MOMENT, education.getStatus());
    }

    @Test
    @DisplayName("Установка и получение названия уч. заведения")
    void testInstitution() {
        String nameOfInstitution = "ПТПП";

        assertNull(education.getNameOfInstitution());

        education.setNameOfInstitution(nameOfInstitution);

        assertEquals(nameOfInstitution, education.getNameOfInstitution());
    }

    @Test
    @DisplayName("Started не может быть в будущем")
    void testStartedValidationFuture() {
        LocalDate future = LocalDate.now().plusDays(1);
        education.setStarted(future);

        var violations = validator.validate(education);
        assertFalse(violations.isEmpty());
    }


    @Test
    @DisplayName("Started может быть в прошлом")
    void testStartedValidationPast() {
        LocalDate past = LocalDate.now().minusDays(1);
        education.setStarted(past);

        var violation = validator.validate(education);
        assertTrue(violation.isEmpty());
    }

    @Test
    @DisplayName("Started может быть сегодня")
    void testStartedValidationToday() {
        LocalDate today = LocalDate.now();
        education.setStarted(today);

        var violation = validator.validate(education);
        assertTrue(violation.isEmpty());
    }

    @Test
    @DisplayName("Все поля корректно работают с null")
    void testNullValues() {
        education.setStarted(null);
        education.setUntil(null);
        education.setType(null);
        education.setStatus(null);
        education.setNameOfInstitution(null);

        assertAll(
                () -> assertNull(education.getStarted()),
                () -> assertNull(education.getUntil()),
                () -> assertNull(education.getType()),
                () -> assertNull(education.getStatus()),
                () -> assertNull(education.getNameOfInstitution())
        );
    }
}
