package com.company.vzvod.service;

import com.company.vzvod.entity.Dep;
import com.company.vzvod.entity.Department;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class DepartmentConverter {

    public static Dep convert(Department department) {
        Dep dep = null;
        if (department != null) {
            dep = Dep.fromId(department.getNumber());
        }
        return dep;
    }

    public static Dep departmentFromDate(LocalDate date) {

        LocalDate startDate = LocalDate.of(2026, 2, 22);

        long daysBetween = ChronoUnit.DAYS.between(startDate, date);
        int dep = 0;

        int normalizedDays = (int) ((daysBetween % 4 + 4) % 4);

        switch (normalizedDays) {
            case 0, 1:
                dep = 1;
                break;
            case 2, 3:
                dep = 2;
                break;
        }

        return Dep.fromId(dep);
    }
}
