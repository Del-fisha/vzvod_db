package com.company.vzvod.bot.dto;

/**
 * Тело {@code POST /api/bot/auth}: номер с любым поддерживаемым префиксом и идентификатор Telegram-чата.
 */
public record BotAuthRequest(String phoneNumber, long chatId) {
}
