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

        assertTrue(normalizer.normalize(phone));
    }

    @Test
    void shouldChangeToPlus7ForElevenDigitsNumber() {

        String result1 = ("79218693457");

    }
}
