package com.company.vzvod.bot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Тело {@code PUT /api/bot/me/profile}: только разрешённые для бота поля; {@code null} — не менять.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BotProfilePatchRequest(String breastplate, Boolean medicalExamination) {

    public boolean hasAnyField() {
        return (breastplate != null && !breastplate.isBlank()) || medicalExamination != null;
    }
}
