package com.company.vzvod.dashboard.todayshift;

/**
 * Показатели одного активного маршрута (смены) для таблицы «Детали».
 */
public record RouteDetailsRow(
        String routeLabel,
        int ap188,
        int ap201,
        int ap2020,
        int ap2021,
        int apOther,
        int upFederalWanted,
        int upWatchList,
        int upLocalSearch,
        int upIdentification,
        int upHotPursuit,
        int ibdr,
        int statements
) {
}
