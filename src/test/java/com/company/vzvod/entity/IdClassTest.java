package com.company.vzvod.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Unit-тесты для IdCard Entity")
public class IdClassTest extends EntityTestSupport {

    private IdCard idCard;

    @BeforeEach
    void setUp() {
        idCard = dataManager.create(IdCard.class);
    }

    @Test
    @DisplayName("Проверка установки и получения ID")
    void testId() {
        UUID originalId = idCard.getId();
        assertNotNull(originalId);

        UUID newId = UUID.randomUUID();
        idCard.setId(newId);
        assertEquals(newId, idCard.getId());
    }
}
