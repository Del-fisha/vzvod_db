package com.company.vzvod.dashboard.todayshift;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Строка таблицы проверок маршрутов на дашборде смены.
 */
public record RouteChecksRow(
        String routeLabel,
        List<RouteCheckEntry> checks
) {
    public record RouteCheckEntry(
            UUID id,
            LocalTime checkedAt,
            String checkerLabel,
            String display
    ) {
    }
}
