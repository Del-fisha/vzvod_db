package com.company.vzvod.service;

import com.company.vzvod.entity.Dep;
import com.company.vzvod.entity.Department;

public class DepartmentConverter {

    public static Dep convert(Department department) {
        Dep dep = null;
        if (department != null) {
            dep = Dep.fromId(department.getNumber());
        }
        return dep;
    }
}
