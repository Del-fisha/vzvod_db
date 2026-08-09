package com.company.vzvod.bot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.List;

/**
 * Ответ {@code GET /api/mobile/me/today-dashboard}: сводка по маршрутам текущих операционных суток.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BotTodayDashboardResponse(
        LocalDate operationalDate,
        int departmentNumber,
        List<BotTodayRouteRow> routes,
        BotTodayTotals totals
) {
}
