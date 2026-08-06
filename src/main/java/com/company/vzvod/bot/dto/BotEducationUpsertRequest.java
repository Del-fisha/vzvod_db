package com.company.vzvod.bot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BotEducationUpsertRequest(
        LocalDate started,
        LocalDate until,
        String typeId,
        String nameOfInstitution
) {
}
