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

    /**
     * Базовый URL микросервиса Telegram-бота (для закрытия сессий). Пусто — уведомления боту не отправляются.
     */
    private String baseUrl = "";

    /**
     * Токен {@code X-Internal-Token} для внутренних вызовов бота (тот же, что {@code internal.api.token} у бота).
     */
    private String internalToken = "";

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey == null ? "" : apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl == null ? "" : baseUrl;
    }

    public String getInternalToken() {
        return internalToken;
    }

    public void setInternalToken(String internalToken) {
        this.internalToken = internalToken == null ? "" : internalToken;
    }
}
