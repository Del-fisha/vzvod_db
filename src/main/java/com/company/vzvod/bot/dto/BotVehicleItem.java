package com.company.vzvod.bot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BotVehicleItem(
        UUID id,
        String stateNumber,
        String brand,
        String model,
        String registrationCertificate,
        LocalDate insurance
) {
}
