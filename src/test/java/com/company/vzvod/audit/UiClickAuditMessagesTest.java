package com.company.vzvod.audit;

import com.vaadin.flow.component.button.Button;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Сообщения аудита кликов UI")
class UiClickAuditMessagesTest {

    @Test
    @DisplayName("формат сообщения о нажатии кнопки")
    void buttonClick_includesActorLabelAndView() {
        String message = UiClickAuditMessages.buttonClick(
                "Иванов И. О.",
                "Сохранить",
                "у сотрудника Петров П. П."
        );
        assertThat(message)
                .isEqualTo("Иванов И. О. нажал кнопку \"Сохранить\" у сотрудника Петров П. П.");
    }

    @Test
    @DisplayName("подпись кнопки из текста, title или id")
    void resolveButtonLabel_prefersText() {
        Button button = new Button();
        button.setText("Отправить");
        assertThat(VaadinButtonClickAuditListener.resolveButtonLabel(button)).isEqualTo("Отправить");

        button.setText("");
        button.getElement().setAttribute("title", "Подсказка");
        assertThat(VaadinButtonClickAuditListener.resolveButtonLabel(button)).isEqualTo("Подсказка");
    }
}
