package com.company.vzvod.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit-тесты для Shift Entity")
class ShiftTest extends EntityTestSupport {

    Shift shift;

    @BeforeEach
    void setUp() {
        shift = dataManager.create(Shift.class);
    }

    @Test
    @DisplayName("Проверка поля departmentToday")
    void getDepartmentToday() {
    }

    @Test
    @DisplayName("Проверка поля units")
    void getUnits() {
    }

    @Test
    @DisplayName("Проверка поля date")
    void getDate() {
    }

    @Test
    @DisplayName("Проверка поля startTime")
    void getStartTime() {
    }

    @Test
    @DisplayName("Проверка поля endTime")
    void getEndTime() {
    }

    @Test
    @DisplayName("Проверка поля criminalViolations")
    void getCriminalViolations() {
    }

    @Test
    @DisplayName("Проверка поля administrativeViolations")
    void getAdministrativeViolations() {
    }

    @Test
    @DisplayName("Проверка поля countOfStatements")
    void getCountOfStatements() {
    }

    @Test
    @DisplayName("Проверка поля countOfClaims")
    void getCountOfClaims() {
    }

    @Test
    @DisplayName("Проверка поля ibdWithMigrant")
    void getIbdWithMigrant() {
    }

    @Test
    @DisplayName("Проверка поля ibdWithoutMigrant")
    void getIbdWithoutMigrant() {
    }

    @Test
    @DisplayName("Проверка поля typeOfShift")
    void getTypeOfShift() {
        assertNull(shift.getTypeOfShift());

        shift.setTypeOfShift(TypeOfShift.BAT_POST);
        assertEquals(TypeOfShift.BAT_POST, shift.getTypeOfShift());
    }

    @Test
    @DisplayName("Проверка поля number")
    void getNumber() {
        assertNull(shift.getNumber());

        shift.setNumber(NumberOfShift._3);
        assertSame(NumberOfShift._3, shift.getNumber());
    }

    @Test
    @DisplayName("Проверка поля id")
    void getId() {
        UUID originalId = shift.getId();
        assertNotNull(originalId);

        UUID newId = UUID.randomUUID();
        shift.setId(newId);
        assertEquals(newId, shift.getId());
    }
}