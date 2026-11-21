package com.company.vzvod.bot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Строка таблицы проверок за операционные сутки: маршрут и проверки по времени.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BotRouteChecksRow(
        String routeLabel,
        List<BotRouteCheckColumn> checks
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record BotRouteCheckColumn(
            String time,
            String checkerLabel
    ) {
    }
}
