package com.company.vzvod.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhoneNormalizerTest {

    private PhoneNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new PhoneNormalizer();
    }

    @Test
    void shouldAddPlus7ForTenDigitsNumber() {

        String phone = "9875698745";

        assertTrue(normalizer.normalize(phone).startsWith("+7"));
        assertEquals(12, normalizer.normalize(phone).length());
    }

    @Test
    void shouldChangeToPlus7ForElevenDigitsNumber() {

        String phone1 = "79218693457";
        String phone2 = "89122291515";

        assertTrue(normalizer.normalize(phone1).startsWith("+7"));
        assertTrue(normalizer.normalize(phone2).startsWith("+7"));

        assertEquals(12, normalizer.normalize(phone1).length());
        assertEquals(12, normalizer.normalize(phone2).length());
    }
}
