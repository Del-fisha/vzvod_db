package com.company.vzvod.bot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BotAddressResponse(
        String index,
        String city,
        String street,
        String houseNumber,
        String body,
        String flat,
        String typeOfHousing,
        String statusOfHousing,
        /** Краткая строка для отображения в Telegram. */
        String summary
) {
}
