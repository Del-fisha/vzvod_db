package com.company.vzvod.bot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BotVehicleUpsertRequest(
        String stateNumber,
        String brand,
        String model,
        String registrationCertificate,
        LocalDate insurance
) {
}
