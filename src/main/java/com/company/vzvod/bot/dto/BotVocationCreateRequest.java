package com.company.vzvod.bot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BotVocationCreateRequest(
        Integer typeId,
        LocalDate startDate,
        LocalDate endDate,
        Boolean hasDeparture,
        String cityToDrive,
        Integer daysAddedByDeparture
) {
}
