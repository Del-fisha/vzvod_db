package com.company.vzvod.audit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Источник изменения для аудита")
class AuditChangeSourceResolverTest {

    private final AuditChangeSourceResolver resolver = new AuditChangeSourceResolver();

    @AfterEach
    void tearDown() {
        AuditChangeContext.clear();
    }

    @Test
    @DisplayName("явная причина из контекста")
    void explicitReason() {
        AuditChangeContext.setReason("синхронизация Telegram");
        assertThat(resolver.resolve()).isEqualTo("Причина: синхронизация Telegram");
    }

    @Test
    @DisplayName("без UI и контекста — программно")
    void programmaticWithoutUi() {
        String source = resolver.resolve();
        assertThat(source).startsWith("Способ: программно —");
    }
}
