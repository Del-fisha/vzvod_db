package com.company.vzvod.bot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BotShiftItem(
        UUID id,
        LocalDate date,
        /** Например «МП 28». */
        String route,
        String shiftType,
        /** Отделение на смену (как в карточке). */
        String departmentToday,
        LocalTime startTime,
        LocalTime endTime
) {
}
