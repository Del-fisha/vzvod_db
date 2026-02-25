package com.company.vzvod.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
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
    void testDepartmentToday() {
        assertNull(shift.getDepartmentToday());

        shift.setDepartmentToday(Dep.FIRST);
        assertEquals(Dep.FIRST, shift.getDepartmentToday());
    }

    @Test
    @DisplayName("Проверка поля units")
    void testUnits() {
        assertNull(shift.getUnits());

        shift.setUnits(new HashSet<>());
        assertTrue(shift.getUnits().isEmpty());

        ServiceInfo unit1 = dataManager.create(ServiceInfo.class);
        ServiceInfo unit2 = dataManager.create(ServiceInfo.class);
        ServiceInfo unit3 = dataManager.create(ServiceInfo.class);

        shift.getUnits().add(unit1);
        shift.getUnits().add(unit2);

        assertTrue(shift.getUnits().contains(unit1));
        assertEquals(2, shift.getUnits().size());

        shift.getUnits().add(unit3);
        assertTrue(shift.getUnits().contains(unit3));
        assertEquals(3, shift.getUnits().size());
    }

    @Test
    @DisplayName("Проверка поля date")
    void testDate() {
        assertNull(shift.getDate());

        LocalDate date = LocalDate.now().minusDays(4);
        shift.setDate(date);

        assertEquals(date, shift.getDate());
    }

    @Test
    @DisplayName("Проверка поля startTime")
    void testStartTime() {
        assertNull(shift.getStartTime());

        LocalTime endTime = LocalTime.now().minusHours(4);
        shift.setStartTime(endTime);

        assertEquals(endTime, shift.getStartTime());
    }

    @Test
    @DisplayName("Проверка поля endTime")
    void testEndTime() {
        assertNull(shift.getEndTime());

        LocalTime endTime = LocalTime.now().minusHours(4);
        shift.setEndTime(endTime);

        assertEquals(endTime, shift.getEndTime());
    }

    @Test
    @DisplayName("Проверка поля criminalViolations")
    void testCriminalViolations() {
        assertNull(shift.getCriminalViolations());

        shift.setCriminalViolations(new HashSet<>());
        assertTrue(shift.getCriminalViolations().isEmpty());

        CriminalViolation violation1 = dataManager.create(CriminalViolation.class);
        CriminalViolation violation2 = dataManager.create(CriminalViolation.class);
        CriminalViolation violation3 = dataManager.create(CriminalViolation.class);

        shift.getCriminalViolations().add(violation1);
        shift.getCriminalViolations().add(violation2);

        assertTrue(shift.getCriminalViolations().contains(violation1));
        assertEquals(2, shift.getCriminalViolations().size());

        shift.getCriminalViolations().add(violation3);
        assertTrue(shift.getCriminalViolations().contains(violation3));
        assertEquals(3, shift.getCriminalViolations().size());
    }

    @Test
    @DisplayName("Проверка поля administrativeViolations")
    void testAdministrativeViolations() {
        assertNull(shift.getAdministrativeViolations());

        shift.setAdministrativeViolations(new HashSet<>());
        assertTrue(shift.getAdministrativeViolations().isEmpty());

        AdministrativeViolation violation1 = dataManager.create(AdministrativeViolation.class);
        AdministrativeViolation violation2 = dataManager.create(AdministrativeViolation.class);
        AdministrativeViolation violation3 = dataManager.create(AdministrativeViolation.class);

        shift.getAdministrativeViolations().add(violation1);
        shift.getAdministrativeViolations().add(violation2);

        assertTrue(shift.getAdministrativeViolations().contains(violation1));
        assertEquals(2, shift.getAdministrativeViolations().size());

        shift.getAdministrativeViolations().add(violation3);
        assertTrue(shift.getAdministrativeViolations().contains(violation3));
        assertEquals(3, shift.getAdministrativeViolations().size());
    }

    @Test
    @DisplayName("Проверка поля countOfStatements")
    void testCountOfStatements() {
        assertNull(shift.getCountOfStatements());

        Integer num = 10;
        shift.setCountOfStatements(num);
        assertEquals(num, shift.getCountOfStatements());
    }

    @Test
    @DisplayName("Проверка поля countOfClaims")
    void testCountOfClaims() {
        assertNull(shift.getCountOfClaims());

        Integer num = 10;
        shift.setCountOfClaims(num);
        assertEquals(num, shift.getCountOfClaims());
    }

    @Test
    @DisplayName("Проверка поля ibdWithMigrant")
    void testIbdWithMigrant() {
        assertNull(shift.getIbdWithMigrant());

        Integer num = 10;
        shift.setIbdWithMigrant(num);
        assertEquals(num, shift.getIbdWithMigrant());
    }

    @Test
    @DisplayName("Проверка поля ibdWithoutMigrant")
    void testIbdWithoutMigrant() {
        assertNull(shift.getIbdWithoutMigrant());

        Integer num = 10;
        shift.setIbdWithoutMigrant(num);
        assertEquals(num, shift.getIbdWithoutMigrant());
    }

    @Test
    @DisplayName("Проверка поля typeOfShift")
    void testTypeOfShift() {
        assertNull(shift.getTypeOfShift());

        shift.setTypeOfShift(TypeOfShift.BAT_POST);
        assertEquals(TypeOfShift.BAT_POST, shift.getTypeOfShift());
    }

    @Test
    @DisplayName("Проверка поля number")
    void testNumber() {
        assertNull(shift.getNumber());

        shift.setNumber(NumberOfShift._3);
        assertSame(NumberOfShift._3, shift.getNumber());
    }

    @Test
    @DisplayName("Проверка поля id")
    void testId() {
        UUID originalId = shift.getId();
        assertNotNull(originalId);

        UUID newId = UUID.randomUUID();
        shift.setId(newId);
        assertEquals(newId, shift.getId());
    }
}