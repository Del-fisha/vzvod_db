package com.company.vzvod.bot.dto;

import java.util.UUID;

public record BotAuthResponse(UUID userId, String displayName) {
}
