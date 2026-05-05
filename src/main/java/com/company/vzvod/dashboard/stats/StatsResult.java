package com.company.vzvod.dashboard.stats;

import java.util.List;

/**
 * Ответ на {@link StatsQuery}: подписи по оси времени + ряды.
 * Итоги по сотруднику заполняются только при периоде не «Сегодня» и ровно одном выбранном сотруднике.
 */
public record StatsResult(
        List<String> bucketLabels,
        List<StatsSeries> series,
        EmployeePeriodTotals employeeTotalsOrNull
) {
    public static StatsResult empty() {
        return new StatsResult(List.of(), List.of(), null);
    }
}
