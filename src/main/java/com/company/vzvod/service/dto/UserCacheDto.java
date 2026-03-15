package com.company.vzvod.service.dto;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public record UserCacheDto(
        UUID id,
        String username,
        String firstName,
        String lastName,
        String patronymic,
        LocalDate dateOfBirth
) implements Serializable {
}