package com.company.vzvod.view.alltodayshifts;

import com.company.vzvod.entity.Dep;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Query-параметры экрана дашборда за выбранный день.
 */
public record DayShiftDashboardParams(LocalDate date, Dep department) {

    public static Optional<DayShiftDashboardParams> from(Map<String, List<String>> queryParameters) {
        if (queryParameters == null) {
            return Optional.empty();
        }
        List<String> dateValues = queryParameters.get("date");
        List<String> departmentValues = queryParameters.get("department");
        if (dateValues == null || dateValues.isEmpty()
                || departmentValues == null || departmentValues.isEmpty()) {
            return Optional.empty();
        }
        try {
            LocalDate date = LocalDate.parse(dateValues.get(0));
            Dep department = Dep.fromId(Integer.valueOf(departmentValues.get(0)));
            if (department == null) {
                return Optional.empty();
            }
            return Optional.of(new DayShiftDashboardParams(date, department));
        } catch (DateTimeParseException | NumberFormatException ex) {
            return Optional.empty();
        }
    }
}
