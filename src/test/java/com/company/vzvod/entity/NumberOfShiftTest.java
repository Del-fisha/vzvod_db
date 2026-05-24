package com.company.vzvod.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NumberOfShiftTest {

    @Test
    void defaultTypeOfShift_mapsRoutesToShiftTypes() {
        assertEquals(TypeOfShift.VZVOD_ROUTE, NumberOfShift._28.defaultTypeOfShift());
        assertEquals(TypeOfShift.VZVOD_ROUTE, NumberOfShift._30.defaultTypeOfShift());
        assertEquals(TypeOfShift.VZVOD_ROUTE, NumberOfShift._31.defaultTypeOfShift());
        assertEquals(TypeOfShift.VZVOD_ROUTE, NumberOfShift._32.defaultTypeOfShift());

        assertEquals(TypeOfShift.BAT_POST, NumberOfShift._5.defaultTypeOfShift());
        assertEquals(TypeOfShift.BAT_POST, NumberOfShift._6.defaultTypeOfShift());
        assertEquals(TypeOfShift.BAT_POST, NumberOfShift._3.defaultTypeOfShift());
        assertEquals(TypeOfShift.BAT_POST, NumberOfShift._18.defaultTypeOfShift());

        assertEquals(TypeOfShift.CHECKING, NumberOfShift.ANOTHER.defaultTypeOfShift());
    }
}
