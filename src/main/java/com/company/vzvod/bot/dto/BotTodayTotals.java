package com.company.vzvod.bot.dto;

import java.util.List;

/**
 * Итоговые показатели операционных суток по отделению для мобильного дашборда.
 */
public record BotTodayTotals(
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
