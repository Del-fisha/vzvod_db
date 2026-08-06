package com.company.vzvod.bot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BotColleagueItem(
        UUID serviceInfoId,
        String label,
        Integer department
) {
}
