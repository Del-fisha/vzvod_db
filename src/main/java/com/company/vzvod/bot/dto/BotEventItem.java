package com.company.vzvod.bot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BotEventItem(
        UUID id,
        LocalDate date,
        LocalTime time,
        String name,
        String place,
        /** Идентификатор {@link com.company.vzvod.entity.EventType#getId()}. */
        String eventType,
        Integer shiftOfDepartment
) {
}
