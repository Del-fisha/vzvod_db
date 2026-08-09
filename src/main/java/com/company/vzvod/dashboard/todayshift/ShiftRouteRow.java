package com.company.vzvod.dashboard.todayshift;

import java.time.LocalTime;

public record ShiftRouteRow(
        String routeLabel,
        String employees,
        LocalTime endTime
) {
}
