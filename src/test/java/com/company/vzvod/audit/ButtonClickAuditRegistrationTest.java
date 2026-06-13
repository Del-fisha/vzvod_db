package com.company.vzvod.audit;

import com.company.vzvod.logging.LoggingServiceClient;
import com.vaadin.flow.component.button.Button;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("Регистрация аудита кликов кнопок")
class ButtonClickAuditRegistrationTest {

    @Mock
    private LoggingServiceClient loggingClient;

    @Mock
    private AuditActorResolver actorResolver;

    @Mock
    private UiClickPageContextResolver pageContextResolver;

    @Test
    @DisplayName("повторная регистрация одной кнопки не дублирует слушатель")
    void registerIfNeeded_isIdempotent() {
        VaadinButtonClickAuditListener listener = new VaadinButtonClickAuditListener(
                loggingClient,
                actorResolver,
                pageContextResolver
        );
        Button button = new Button("Сохранить");

        listener.registerIfNeeded(button);
        listener.registerIfNeeded(button);

        assertThat(button.getElement().getAttribute("data-audit-click-registered")).isEqualTo("true");
    }
}
