package com.company.vzvod.messaging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Форматирование текста дашборд-сообщения")
class DashboardMessageFormatterTest {

    @Test
    @DisplayName("Добавляет строку отправителя и пустую строку перед текстом")
    void formatsSenderAndBody() {
        String formatted = DashboardMessageFormatter.formatTelegramBody("Иванов И.И.", "Первая строка\nВторая строка");

        assertEquals("Отправитель: Иванов И.И.\n\nПервая строка\nВторая строка", formatted);
    }
}
