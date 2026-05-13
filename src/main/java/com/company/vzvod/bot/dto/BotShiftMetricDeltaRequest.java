package com.company.vzvod.bot.dto;

/**
 * Тело POST-запросов на изменение показателей открытой смены: {@code +1} или {@code -1}.
 */
public record BotShiftMetricDeltaRequest(int delta) {
}
