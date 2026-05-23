package com.company.vzvod.dashboard.todayshift;

import java.time.LocalDate;
import java.util.List;

public record TodayShiftDashboardSnapshot(
        LocalDate operationalDate,
        int departmentNumber,
        List<ShiftRouteRow> routes,
        int totalIbdWithMigrant,
        int totalStatements,
        int totalClaims,
        int totalAdministrativeViolations,
        int totalCriminalViolations,
        List<ArticleCountRow> administrativeByArticle,
        List<CriminalTypeCountRow> criminalByType,
        List<RouteDetailsRow> routeDetails,
        List<PeriodMetricRow> periodMetrics
) {
}
