package com.company.vzvod.service;

import com.company.vzvod.entity.Dep;
import com.company.vzvod.entity.Department;

public class DepartmentConverter {

    public static Dep convert(Department department) {
        Dep dep = null;
        if (department != null) {
            return Dep.fromId(department.getNumber());
        } else return Dep.fromId(1);
    }
}
