package com.company.vzvod.service;

import com.company.vzvod.entity.Department;
import io.jmix.core.DataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
public class DepartmentInitializer {

    @Autowired
    private DataManager dataManager;

    @PostConstruct
    public void initDepartments() {
        Long count1 = dataManager
                .loadValue("select count(d) from Department d where d.number = 1", Long.class).one();

        if (count1 == 0) {
            Department dept1 = dataManager.create(Department.class);
            dept1.setNumber(1);
            dataManager.save(dept1);
        }

        Long count2 = dataManager
                .loadValue("select count(d) from Department d where d.number = 2", Long.class).one();

        if (count2 == 0) {
            Department dept2 = dataManager.create(Department.class);
            dept2.setNumber(2);
            dataManager.save(dept2);
        }
    }
}
