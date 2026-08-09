package com.company.vzvod.bot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Тело {@code POST|PUT /api/bot/me/shifts} и {@code /api/mobile/me/shifts}.
 * Напарники: {@code partnerServiceInfoId} и/или {@code partnerServiceInfoIds} (несколько).
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
        /** Один напарник (совместимость со старым клиентом/ботом). */
        UUID partnerServiceInfoId,
        Integer countOfStatements,
        Integer countOfClaims,
        Integer ibdr,
        Integer migrant,
        /** Несколько напарников; объединяется с {@link #partnerServiceInfoId}. */
        List<UUID> partnerServiceInfoIds
) {
}
