package com.company.vzvod.bot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Ответ {@code GET /api/bot/me/profile} для Telegram-бота.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BotProfileResponse(
        UUID userId,
        String displayName,
        String rank,
        String post,
        String department,
        String breastplate,
        Boolean medicalExamination,
        LocalDate idCardIssued,
        LocalDate idCardUntil
) {
}
