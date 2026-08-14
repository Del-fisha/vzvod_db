package com.company.vzvod.bot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BotRouteCheckCreateRequest(
        /** {@link com.company.vzvod.entity.NumberOfShift#getId()} */
        String routeId
) {
}
