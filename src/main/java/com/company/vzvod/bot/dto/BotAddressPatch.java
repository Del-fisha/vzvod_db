package com.company.vzvod.bot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Частичное обновление адреса в {@link BotProfilePatchRequest}: {@code null} — не менять поле.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BotAddressPatch(
        String index,
        String city,
        String street,
        String houseNumber,
        String body,
        String flat,
        /** Идентификатор {@link com.company.vzvod.entity.TypeOfHousing} ({@code A}/{@code B}/{@code C}). */
        String typeOfHousing,
        /** Идентификатор {@link com.company.vzvod.entity.StatusOfHousing} ({@code A}…{@code D}). */
        String statusOfHousing
) {
    public boolean hasAny() {
        return nonBlank(index) || nonBlank(city) || nonBlank(street) || nonBlank(houseNumber)
                || nonBlank(body) || nonBlank(flat) || nonBlank(typeOfHousing) || nonBlank(statusOfHousing);
    }

    private static boolean nonBlank(String s) {
        return s != null && !s.isBlank();
    }
}
