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
        String nearestMetroLabel,
        /** Командир отделения, командир взвода или заместитель командира взвода. */
        Boolean fullAccess,
        LocalDate dateOfBirth,
        /** Личный номер БШ ({@code ServiceInfo.token}). */
        String token,
        /** Номер СПЛ служебного удостоверения. */
        String spl,
        /** Чистая выслуга на сегодня: «N лет, M месяцев». */
        String seniorityLabel,
        /** Полный номер для карточки, {@code +7 (999) 999-99-99}. */
        String mobilePhone,
        Integer departmentNumber
) {
}
