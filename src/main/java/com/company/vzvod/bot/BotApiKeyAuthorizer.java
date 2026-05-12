package com.company.vzvod.bot;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class BotApiKeyAuthorizer {

    private final BotApiProperties botApiProperties;

    public BotApiKeyAuthorizer(BotApiProperties botApiProperties) {
        this.botApiProperties = botApiProperties;
    }

    public void verify(String providedApiKeyHeader) {
        String expected = botApiProperties.getApiKey();
        if (expected.isBlank()) {
            return;
        }
        if (providedApiKeyHeader == null || !constantTimeEquals(expected, providedApiKeyHeader)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    private static boolean constantTimeEquals(String expected, String actual) {
        byte[] e = expected.getBytes(StandardCharsets.UTF_8);
        byte[] a = actual.getBytes(StandardCharsets.UTF_8);
        if (e.length != a.length) {
            return false;
        }
        return MessageDigest.isEqual(e, a);
    }
}
