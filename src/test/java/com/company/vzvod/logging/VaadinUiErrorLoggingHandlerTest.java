package com.company.vzvod.logging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Логирование ошибок Vaadin UI")
class VaadinUiErrorLoggingHandlerTest {

    @Test
    @DisplayName("формат сообщения содержит текст исключения")
    void formatUiError_includesMessage() {
        String formatted = VaadinUiErrorLoggingHandler.formatUiError(
                new NullPointerException("userReadService is null")
        );
        assertThat(formatted)
                .startsWith("Ошибка UI:")
                .contains("userReadService is null")
                .contains("at ");
    }
}
