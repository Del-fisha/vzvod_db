package com.company.vzvod.bot.dto;

import java.util.List;

public record BotViolationOptionsResponse(
        List<BotEnumOption> impacts,
        List<BotEnumOption> administrativeArticles,
        List<BotEnumOption> criminalTypes
) {
}
