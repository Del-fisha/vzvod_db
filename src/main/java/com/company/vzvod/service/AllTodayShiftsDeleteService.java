package com.company.vzvod.service;

import com.company.vzvod.entity.AdministrativeViolation;
import com.company.vzvod.entity.AllTodayShifts;
import com.company.vzvod.entity.CriminalViolation;
import com.company.vzvod.entity.Dep;
import com.company.vzvod.entity.Shift;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class AllTodayShiftsDeleteService {

    private final DataManager dataManager;

    public AllTodayShiftsDeleteService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Transactional
    public void deleteShifts(Collection<Shift> shifts) {
        if (shifts == null || shifts.isEmpty()) {
            return;
        }

        Set<UUID> shiftIds = new HashSet<>();
        Set<String> dayKeys = new HashSet<>();
        SaveContext saveContext = new SaveContext();

        for (Shift shift : shifts) {
            if (shift == null || shift.getId() == null) {
                continue;
            }
            Shift loaded = dataManager.load(Shift.class)
                    .id(shift.getId())
                    .fetchPlan(f -> f.add("date").add("departmentToday"))
                    .optional()
                    .orElse(null);
            if (loaded == null || !shiftIds.add(loaded.getId())) {
                continue;
            }

            LocalDate date = loaded.getDate();
            Dep department = loaded.getDepartmentToday();
            if (date != null && department != null) {
                dayKeys.add(dayKey(date, department));
            }

            removeViolations(loaded.getId(), saveContext);
            saveContext.removing(loaded);
        }

        if (shiftIds.isEmpty()) {
            return;
        }

        dataManager.save(saveContext);
        removeEmptyDays(dayKeys);
    }

    private void removeViolations(UUID shiftId, SaveContext saveContext) {
        dataManager.load(AdministrativeViolation.class)
                .query("select v from AdministrativeViolation v where v.shift.id = :shiftId")
                .parameter("shiftId", shiftId)
                .list()
                .forEach(saveContext::removing);

        dataManager.load(CriminalViolation.class)
                .query("select v from CriminalViolation v where v.shift.id = :shiftId")
                .parameter("shiftId", shiftId)
                .list()
                .forEach(saveContext::removing);
    }

    private void removeEmptyDays(Set<String> dayKeys) {
        for (String key : dayKeys) {
            String[] parts = key.split("\\|", 2);
            LocalDate date = LocalDate.parse(parts[0]);
            Dep department = Dep.fromId(Integer.valueOf(parts[1]));
            if (department == null) {
                continue;
            }

            Long remaining = dataManager.loadValue(
                            "select count(e) from Shift e where e.date = :date and e.departmentToday = :department",
                            Long.class)
                    .parameter("date", date)
                    .parameter("department", department.getId())
                    .one();
            if (remaining != null && remaining > 0) {
                continue;
            }

            dataManager.load(AllTodayShifts.class)
                    .query("select e from AllTodayShifts e where e.date = :date and e.department = :department")
                    .parameter("date", date)
                    .parameter("department", department.getId())
                    .list()
                    .forEach(dataManager::remove);
        }
    }

    private static String dayKey(LocalDate date, Dep department) {
        return date + "|" + Objects.requireNonNull(department.getId());
    }
}
