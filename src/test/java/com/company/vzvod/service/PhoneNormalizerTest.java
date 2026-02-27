package com.company.vzvod.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PhoneNormalizerTest {

    private PhoneNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new PhoneNormalizer();
    }

    @Test
    void shouldAddPlus7ForTenDigitsNumber() {

        String phone = "9875698745";

        assertTrue(normalizer.normalize(phone)); // ToDo startWith +7
        assertTrue(normalizer.normalize(phone)); // ToDo len = 12
    }

    @Test
    void shouldChangeToPlus7ForElevenDigitsNumber() {

        String phone1 = "79218693457";
        String phone2 = "89122291515";

        assertTrue(normalizer.normalize(phone1)); // ToDo startWith +7
        assertTrue(normalizer.normalize(phone2)); // ToDo startWith +7

        assertTrue(normalizer.normalize(phone1)); // ToDo len = 12
        assertTrue(normalizer.normalize(phone2)); // ToDo len = 12


    }
}
