package com.company.vzvod.bot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BotCheckableRoutesResponse(
        List<BotCheckableRouteItem> routes,
        List<BotRouteCheckerOption> commanders
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record BotCheckableRouteItem(
            String routeId,
            String routeLabel,
            List<BotRouteCheckItem> checks
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record BotRouteCheckItem(
            UUID id,
            LocalTime checkedAt,
            String checkerLabel,
            String display,
            UUID checkerServiceInfoId
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record BotRouteCheckerOption(
            UUID serviceInfoId,
            String label
    ) {
    }
}
