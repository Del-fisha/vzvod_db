package com.company.vzvod.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VehicleStateNumberServiceTest {

    private final VehicleStateNumberService service = new VehicleStateNumberService();

    @Test
    void normalize_shouldFormatAndUppercase() {
        assertEquals("У-234-КА_093", service.normalize("У234КА093"));
        assertEquals("К-598-НР_65", service.normalize("К  5 -98_НР6 5"));
        assertEquals("Н-542-СС_651", service.normalize("Н____54     2_СС....65,1"));
    }

    @Test
    void isValid_shouldAcceptNoisyInput() {
        assertTrue(service.isValid("Н____54     2_СС....65,1"));
        assertTrue(service.isValid("у-234-ка_093"));
        assertFalse(service.isValid("123ABC"));
    }
}

