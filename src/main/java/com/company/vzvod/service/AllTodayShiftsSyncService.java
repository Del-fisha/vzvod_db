package com.company.vzvod.service;

import com.company.vzvod.entity.AllTodayShifts;
import com.company.vzvod.entity.Dep;
import com.company.vzvod.entity.Shift;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AllTodayShiftsSyncService {

    private final DataManager dataManager;

    public AllTodayShiftsSyncService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Transactional
    public AllTodayShifts ensureExists(LocalDate date, Dep department) {
        if (date == null || department == null) {
            return null;
        }

        AllTodayShifts existing = findByDateAndDepartment(date, department);
        if (existing != null) {
            return existing;
        }

        AllTodayShifts created = dataManager.create(AllTodayShifts.class);
        created.setDate(date);
        created.setDepartment(department);
        return dataManager.save(created);
    }

    /**
     * По всем Shift собирает уникальные пары (дата, отделение) и создаёт недостающие строки.
     *
     * @return число вновь созданных записей
     */
    @Transactional
    public int syncFromShifts() {
        List<Shift> shifts = dataManager.load(Shift.class)
                .query("select e from Shift e where e.date is not null")
                .fetchPlan(f -> f.add("date").add("departmentToday"))
                .list();

        Set<String> seen = new HashSet<>();
        SaveContext saveContext = new SaveContext();
        int created = 0;

        for (Shift shift : shifts) {
            LocalDate date = shift.getDate();
            Dep department = shift.getDepartmentToday();
            if (date == null || department == null) {
                continue;
            }
            String key = date + "|" + department.getId();
            if (!seen.add(key)) {
                continue;
            }
            if (findByDateAndDepartment(date, department) != null) {
                continue;
            }
            AllTodayShifts row = dataManager.create(AllTodayShifts.class);
            row.setDate(date);
            row.setDepartment(department);
            saveContext.saving(row);
            created++;
        }

        if (created > 0) {
            dataManager.save(saveContext);
        }
        return created;
    }

    private AllTodayShifts findByDateAndDepartment(LocalDate date, Dep department) {
        return dataManager.load(AllTodayShifts.class)
                .query("select e from AllTodayShifts e where e.date = :date and e.department = :dep")
                .parameter("date", date)
                .parameter("dep", department.getId())
                .optional()
                .orElse(null);
    }
}
