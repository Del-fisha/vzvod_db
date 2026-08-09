package com.company.vzvod.bot.dto;

import java.util.List;

public record BotCatalogOptionsResponse(
        List<BotEnumOption> vocationTypes,
        List<BotShiftRouteOption> shiftRoutes,
        List<BotStringEnumOption> shiftTypes
) {
}
