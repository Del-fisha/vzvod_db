package com.company.vzvod.bot.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("BotProfilePatchRequest.hasAnyField")
class BotProfilePatchRequestTest {

    @Test
    @DisplayName("пусто, если ни одно поле не задано")
    void hasAnyField_falseWhenAllNull() {
        BotProfilePatchRequest patch = new BotProfilePatchRequest(null, null, null, null, null);
        assertFalse(patch.hasAnyField());
    }

    @Test
    @DisplayName("true, если задан только nearestMetro")
    void hasAnyField_trueWhenOnlyNearestMetroSet() {
        BotProfilePatchRequest patch = new BotProfilePatchRequest(null, null, null, null, 101);
        assertTrue(patch.hasAnyField());
    }

    @Test
    @DisplayName("true, если задан нагрудный знак")
    void hasAnyField_trueWhenBreastplateSet() {
        BotProfilePatchRequest patch = new BotProfilePatchRequest("ABCDEFGH", null, null, null, null);
        assertTrue(patch.hasAnyField());
    }
}
