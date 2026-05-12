package com.company.vzvod.bot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Тело {@code PUT /api/bot/me/profile}: только разрешённые для бота поля; {@code null} — не менять.
 * Телефон менять нельзя (только через учётную систему).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BotProfilePatchRequest(
        String breastplate,
        Boolean medicalExamination,
        BotAddressPatch registration,
        BotAddressPatch habitation
) {

    public boolean hasAnyField() {
        return (breastplate != null && !breastplate.isBlank())
                || medicalExamination != null
                || (registration != null && registration.hasAny())
                || (habitation != null && habitation.hasAny());
    }
}
