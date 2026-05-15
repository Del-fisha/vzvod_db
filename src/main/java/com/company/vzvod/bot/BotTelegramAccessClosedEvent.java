package com.company.vzvod.bot;

/**
 * Событие после снятия привязки Telegram: уведомить микросервис бота (после commit транзакции).
 */
public record BotTelegramAccessClosedEvent(long chatId, String message) {
}
