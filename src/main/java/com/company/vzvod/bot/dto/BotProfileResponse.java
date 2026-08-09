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
        /** Маска для отображения; изменить номер через бота нельзя. */
        String mobilePhoneMasked,
        BotAddressResponse registration,
        BotAddressResponse habitation,
        LocalDate idCardIssued,
        LocalDate idCardUntil,
        /** Идентификатор {@link com.company.vzvod.entity.MetroStation#getId()} ближайшей станции метро. */
        Integer nearestMetro,
        String nearestMetroLabel
) {
}
