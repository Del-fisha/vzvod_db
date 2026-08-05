package com.company.vzvod.view.alltodayshifts;

/**
 * Куда открывать строку маршрута из списка дня.
 */
public enum RouteRowOpenTarget {
    SHIFT_BLANK,
    SHIFT_DETAIL;

    public static RouteRowOpenTarget forSingleClick() {
        return SHIFT_BLANK;
    }

    public static RouteRowOpenTarget forDoubleClick() {
        return SHIFT_DETAIL;
    }
}
