package com.company.vzvod.bot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BotStaffWorkResponse(
        int year,
        BotWorkTotals month,
        BotWorkTotals yearTotals,
        BotWorkTotals allTime,
        int vacationEntitled,
        int vacationRemaining,
        List<BotVocationItem> vacationsThisYear
) {
}
