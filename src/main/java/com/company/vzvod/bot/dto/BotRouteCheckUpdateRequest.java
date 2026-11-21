package com.company.vzvod.bot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BotRouteCheckUpdateRequest(
        LocalTime checkedAt,
        UUID checkerServiceInfoId
) {
}
