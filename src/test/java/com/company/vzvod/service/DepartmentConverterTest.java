package com.company.vzvod.service;

import com.company.vzvod.entity.Dep;
import com.company.vzvod.entity.Department;
import io.jmix.core.DataManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тест конвертера отделений")
@SpringBootTest
class DepartmentConverterTest {

    @Autowired
    protected DataManager dataManager;

    @Test
    @DisplayName("Тест конвертера")
    void testConvert() {
        Department department1 = dataManager.create(Department.class);
        Department department2 = dataManager.create(Department.class);
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