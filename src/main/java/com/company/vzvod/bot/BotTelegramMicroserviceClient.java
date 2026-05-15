package com.company.vzvod.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class BotTelegramMicroserviceClient {

    private static final Logger log = LoggerFactory.getLogger(BotTelegramMicroserviceClient.class);

    private final RestClient restClient;
    private final String internalToken;
    private final boolean enabled;

    public BotTelegramMicroserviceClient(BotApiProperties properties) {
        String baseUrl = properties.getBaseUrl() == null ? "" : properties.getBaseUrl().trim();
        this.internalToken = properties.getInternalToken() == null ? "" : properties.getInternalToken().trim();
        this.enabled = !baseUrl.isEmpty();
        this.restClient = enabled ? RestClient.builder().baseUrl(baseUrl).build() : null;
    }

    public void revokeAccess(long chatId, String message) {
        if (!enabled) {
            log.debug("vzvod.bot.base-url не задан — пропуск уведомления бота chatId={}", chatId);
            return;
        }
        try {
            RestClient.RequestBodySpec spec = restClient.post()
                    .uri("/internal/telegram/revoke-access")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON);
            if (!internalToken.isEmpty()) {
                spec = spec.header("X-Internal-Token", internalToken);
            }
            spec.body(new RevokeAccessRequest(chatId, message))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("Не удалось уведомить микросервис бота о закрытии доступа chatId={}: {}", chatId, e.toString());
        }
    }

    private record RevokeAccessRequest(long chatId, String message) {
    }
}
