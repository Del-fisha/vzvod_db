package com.company.vzvod.dashboard.todayshift;

/**
 * Строка сводной таблицы «за смену / месяц / год / итого».
 *
 * @param messageKey ключ в message bundle (todayShiftDashboardDialog.period.*)
 * @param sectionHeader если true — заголовок группы без числовых значений
 * @param sumRow если true — итоговая строка (жирный шрифт)
 */
public record PeriodMetricRow(
        String messageKey,
        boolean sectionHeader,
        boolean sumRow,
        int shiftCount,
        int monthCount,
        int yearCount,
        int totalCount
) {
}
