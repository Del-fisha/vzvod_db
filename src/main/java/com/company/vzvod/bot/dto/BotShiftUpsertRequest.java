package com.company.vzvod.bot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Тело {@code POST|PUT /api/bot/me/shifts}: поля смены как в карточке «Моя смена».
 * Счётчики опциональны — по умолчанию 0, как при создании в UI.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BotShiftUpsertRequest(
        LocalDate date,
        /** Идентификатор маршрута {@link com.company.vzvod.entity.NumberOfShift#getId()}. */
        String routeId,
        /** Идентификатор типа {@link com.company.vzvod.entity.TypeOfShift#getId()}. */
        String typeOfShiftId,
        LocalTime startTime,
        /** Задаётся позже при завершении смены; для POST/PUT может быть null. */
        LocalTime endTime,
        /** Второй участник смены (служба в отделении 1 или 2); обязателен при создании через бота. */
        UUID partnerServiceInfoId,
        Integer countOfStatements,
        Integer countOfClaims,
        Integer ibdWithMigrant,
        Integer ibdWithoutMigrant
) {
}
