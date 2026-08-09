package com.company.vzvod.dashboard.stats;

import java.util.UUID;

/**
 * Суммы за весь выбранный период (не «по точкам графика») для одного сотрудника.
 *
 * @param employeeUserId сотрудник, для которого показаны итоги
 */
public record EmployeePeriodTotals(
        UUID employeeUserId,
        long administrativeViolations,
        long criminalViolations,
        long ibdr
) {
}
