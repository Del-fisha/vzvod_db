package com.company.vzvod.bot;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Настройки REST API для микросервиса Telegram-бота.
 */
@ConfigurationProperties(prefix = "vzvod.bot")
public class BotApiProperties {

    /**
     * Ключ в заголовке {@code X-Api-Key}. Пустое значение — только для разработки: проверка ключа отключена.
     */
    private String apiKey = "";

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey == null ? "" : apiKey;
    }
}
