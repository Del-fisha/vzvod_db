package com.company.vzvod.bot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Тело {@code POST /api/mobile/me/events} (и {@code POST /api/bot/me/events}): создание мероприятия.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BotEventCreateRequest(
        String name,
        LocalDate date,
        LocalTime time,
        String place,
        String description,
        /** Идентификатор {@link com.company.vzvod.entity.EventType#getId()}; {@code null} — по умолчанию OTHER. */
        String eventType
) {
}
