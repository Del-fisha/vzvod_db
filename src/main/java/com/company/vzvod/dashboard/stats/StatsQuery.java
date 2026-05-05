package com.company.vzvod.dashboard.stats;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Универсальный запрос статистики (вариант A).
 * <p>
 * Пустые {@code administrativeArticleIds} / {@code criminalTypeIds} означают «все значения» (без фильтра).
 * <p>
 * При множественном выборе сотрудников блок итогов за период в UI не показывается; для графика используются все выбранные id.
 */
public record StatsQuery(
        StatsPeriod period,
        LocalDate referenceDate,
        EnumSet<WorkMetric> metrics,
        Set<Integer> administrativeArticleIds,
        Set<Integer> criminalTypeIds,
        StatsCompareMode compareMode,
        Set<UUID> departmentIds,
        Set<UUID> employeeUserIds
) {
    public StatsQuery {
        Objects.requireNonNull(period, "period");
        metrics = metrics == null ? EnumSet.noneOf(WorkMetric.class) : EnumSet.copyOf(metrics);
        administrativeArticleIds = administrativeArticleIds == null ? Set.of() : Set.copyOf(administrativeArticleIds);
        criminalTypeIds = criminalTypeIds == null ? Set.of() : Set.copyOf(criminalTypeIds);
        Objects.requireNonNull(compareMode, "compareMode");
        departmentIds = departmentIds == null ? Set.of() : Set.copyOf(departmentIds);
        employeeUserIds = employeeUserIds == null ? Set.of() : Set.copyOf(employeeUserIds);
    }

    public void validate() {
        if (metrics.isEmpty()) {
            throw new IllegalArgumentException("At least one metric required");
        }
        if (compareMode == StatsCompareMode.DEPARTMENTS && departmentIds.isEmpty()) {
            throw new IllegalArgumentException("Department ids required for DEPARTMENTS mode");
        }
        if (compareMode == StatsCompareMode.EMPLOYEES && employeeUserIds.isEmpty()) {
            throw new IllegalArgumentException("Employee user ids required for EMPLOYEES mode");
        }
    }

    /** Дата для границ «сегодня / месяц / год»; если null во входе сервиса — подставляется сегодня. */
    public LocalDate resolveReferenceDate() {
        return referenceDate != null ? referenceDate : LocalDate.now();
    }
}
