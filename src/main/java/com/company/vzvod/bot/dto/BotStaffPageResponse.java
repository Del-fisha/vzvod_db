package com.company.vzvod.bot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BotStaffPageResponse(
        List<BotStaffItem> items,
        int page,
        boolean hasMore
) {
}
