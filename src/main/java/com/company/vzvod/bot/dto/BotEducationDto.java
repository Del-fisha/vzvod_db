package com.company.vzvod.bot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BotEducationDto(
        UUID id,
        LocalDate started,
        LocalDate until,
        String typeId,
        String typeLabel,
        String statusId,
        String statusLabel,
        String nameOfInstitution
) {
}
