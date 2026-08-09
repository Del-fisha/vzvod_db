package com.company.vzvod.bot.dto;

import java.util.List;

/**
 * Показатели одного маршрута (смены) операционных суток для мобильного дашборда.
 */
public record BotTodayRouteRow(
        String routeLabel,
        List<String> employees,
        int ibdr,
        int migrant,
        int statements,
        int claims,
        int administrative,
        int criminal,
        List<BotLabeledCount> administrativeArticles,
        List<BotLabeledCount> criminalTypes
) {
}
