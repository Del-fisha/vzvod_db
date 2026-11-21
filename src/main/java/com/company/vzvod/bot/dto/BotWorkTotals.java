package com.company.vzvod.bot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BotWorkTotals(
        int ibdr,
        int administrative,
        int criminal
) {
}
