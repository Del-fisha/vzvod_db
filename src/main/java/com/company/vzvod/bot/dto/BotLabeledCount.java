package com.company.vzvod.bot.dto;

/**
 * Подписанный счётчик для разбивки по статьям/типам («20.20» × count, «Федеральный розыск» × count).
 */
public record BotLabeledCount(String label, int count) {
}
