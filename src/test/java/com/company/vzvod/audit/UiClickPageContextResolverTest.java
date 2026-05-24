package com.company.vzvod.audit;

import com.vaadin.flow.router.QueryParameters;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Контекст страницы для аудита кликов")
class UiClickPageContextResolverTest {

    private final UUID userId = UUID.fromString("11111111-2222-3333-4444-555555555555");

    @Test
    @DisplayName("список users без UUID")
    void usersListPath_hasHumanReadableContext() {
        var resolver = new UiClickPageContextResolver(null);
        assertThat(resolver.resolveFromLocation("users", QueryParameters.empty()))
                .isEqualTo("на странице списка сотрудников");
    }

    @Test
    @DisplayName("сообщение клика без UUID в тексте")
    void buttonMessage_usesEmployeeContext() {
        String message = UiClickAuditMessages.buttonClick(
                "Тарасов А. Н.",
                "Служебная информация",
                "у сотрудника Петров П. П."
        );
        assertThat(message)
                .isEqualTo("Тарасов А. Н. нажал кнопку \"Служебная информация\" у сотрудника Петров П. П.")
                .doesNotContain(userId.toString());
    }
}
