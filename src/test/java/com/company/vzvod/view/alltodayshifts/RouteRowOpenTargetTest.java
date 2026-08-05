package com.company.vzvod.view.alltodayshifts;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Открытие маршрута из списка дня")
class RouteRowOpenTargetTest {

    @Test
    @DisplayName("Одиночный клик открывает ShiftBlank")
    void singleClick_opensBlank() {
        assertEquals(RouteRowOpenTarget.SHIFT_BLANK, RouteRowOpenTarget.forSingleClick());
    }

    @Test
    @DisplayName("Двойной клик открывает ShiftDetail")
    void doubleClick_opensDetail() {
        assertEquals(RouteRowOpenTarget.SHIFT_DETAIL, RouteRowOpenTarget.forDoubleClick());
    }
}
