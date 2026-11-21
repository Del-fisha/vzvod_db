package com.company.vzvod.bot;

import com.company.vzvod.bot.dto.BotProfileResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Поля карточки в JSON профиля")
class ProfileCardFieldsTest {

    @Test
    @DisplayName("телефон в карточку уходит полностью, без маски")
    void phoneIsNotMasked() {
        String phone = BotMeProfileService.formatRussianMobile("+79115557788");
        assertEquals("+7 (911) 555-77-88", phone);
        assertFalse(phone.contains("*"));
    }

    @Test
    @DisplayName("БШ, СПЛ, выслуга и телефон сериализуются в JSON")
    void jsonContainsCardFields() throws Exception {
        BotProfileResponse response = new BotProfileResponse(
                UUID.randomUUID(),
                "Тарасов Алексей Николаевич",
                "Прапорщик",
                "Командир отделения",
                "Отделение № 2",
                "025161",
                true,
                "+7 *** *** 77 88",
                null,
                null,
                LocalDate.of(2026, 5, 13),
                LocalDate.of(2031, 5, 13),
                1,
                "Гражданский проспект",
                true,
                LocalDate.of(1992, 7, 10),
                "65492",
                "123456",
                "10 лет, 4 месяца",
                "+7 (911) 555-77-88",
                2
        );

        String json = new ObjectMapper().findAndRegisterModules().writeValueAsString(response);

        assertTrue(json.contains("\"token\":\"65492\""));
        assertTrue(json.contains("\"spl\":\"123456\""));
        assertTrue(json.contains("\"seniorityLabel\":\"10 лет, 4 месяца\""));
        assertTrue(json.contains("\"mobilePhone\":\"+7 (911) 555-77-88\""));
        assertFalse(json.contains("\"mobilePhone\":\"+7 ***"));
    }
}
