package com.company.vzvod.bot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BotVocationItem(
        LocalDate startDate,
        LocalDate endDate,
        Integer countOfDays,
        String typeLabel
) {
}
