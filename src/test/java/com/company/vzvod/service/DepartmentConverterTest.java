package com.company.vzvod.service;

import com.company.vzvod.entity.Dep;
import com.company.vzvod.entity.Department;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Тест конвертера отделений")
class DepartmentConverterTest {

    @Test
    @DisplayName("Тест конвертера")
    void testConvert() {
        Department department1 = new Department();
        Department department2 = new Department();
        department1.setNumber(1);
        department2.setNumber(2);

        assertEquals(Dep.FIRST, DepartmentConverter.convert(department1));
        assertEquals(Dep.SECOND, DepartmentConverter.convert(department2));
    }

    @Test
    @DisplayName("Тест определения отделения по датам")
    void testDepartmentFromDate() {
        LocalDate date11 = LocalDate.of(2029, 5, 17);
        LocalDate date12 = LocalDate.of(2031, 11, 12);
        LocalDate date21 = LocalDate.of(2026, 7, 6);
        LocalDate date22 = LocalDate.of(2028, 2, 3);

        assertEquals(Dep.FIRST, DepartmentConverter.departmentFromDate(date11));
        assertEquals(Dep.FIRST, DepartmentConverter.departmentFromDate(date12));
        assertEquals(Dep.SECOND, DepartmentConverter.departmentFromDate(date21));
        assertEquals(Dep.SECOND, DepartmentConverter.departmentFromDate(date22));
    }
}
