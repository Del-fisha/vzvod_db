package com.company.vzvod.service;

import com.company.vzvod.dashboard.todayshift.RouteChecksRow;
import com.company.vzvod.entity.NumberOfShift;
import com.company.vzvod.entity.RouteCheck;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.entity.TypeOfShift;
import com.company.vzvod.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RouteCheckService.buildRows")
class RouteCheckServiceBuildRowsTest {

    @Test
    @DisplayName("строит строки только по не-CHECKING маршрутам и сортирует проверки по времени")
    void buildsRowsForNonCheckingRoutes() {
        Shift mp28 = shift(NumberOfShift._28, TypeOfShift.VZVOD_ROUTE);
        Shift mp30 = shift(NumberOfShift._30, TypeOfShift.VZVOD_ROUTE);
        Shift checking = shift(NumberOfShift.ANOTHER, TypeOfShift.CHECKING);

        RouteCheck late = check(NumberOfShift._28, LocalTime.of(15, 0), "Петров", "П", "С");
        RouteCheck early = check(NumberOfShift._28, LocalTime.of(10, 15), "Иванов", "И", "О");

        List<RouteChecksRow> rows = RouteCheckService.buildRows(
                List.of(mp30, checking, mp28),
                List.of(late, early)
        );

        assertEquals(2, rows.size());
        assertEquals("МП 28", rows.get(0).routeLabel());
        assertEquals("МП 30", rows.get(1).routeLabel());
        assertEquals(2, rows.get(0).checks().size());
        assertEquals("10:15\nИванов И. О.", rows.get(0).checks().get(0).display());
        assertEquals("15:00\nПетров П. С.", rows.get(0).checks().get(1).display());
        assertTrue(rows.get(1).checks().isEmpty());
    }

    @Test
    @DisplayName("удалённый маршрут не даёт строку, даже если проверки остались")
    void deletedRouteDoesNotAppear() {
        Shift mp28 = shift(NumberOfShift._28, TypeOfShift.VZVOD_ROUTE);
        RouteCheck orphan = check(NumberOfShift._30, LocalTime.of(11, 0), "Сидоров", "С", "И");

        List<RouteChecksRow> rows = RouteCheckService.buildRows(List.of(mp28), List.of(orphan));

        assertEquals(1, rows.size());
        assertEquals("МП 28", rows.get(0).routeLabel());
        assertTrue(rows.get(0).checks().isEmpty());
    }

    private static Shift shift(NumberOfShift number, TypeOfShift type) {
        Shift s = new Shift();
        s.setId(UUID.randomUUID());
        s.setNumber(number);
        s.setTypeOfShift(type);
        return s;
    }

    private static RouteCheck check(
            NumberOfShift route,
            LocalTime at,
            String last,
            String first,
            String patronymic
    ) {
        User user = new User();
        user.setLastName(last);
        user.setFirstName(first);
        user.setPatronymic(patronymic);
        ServiceInfo si = new ServiceInfo();
        si.setId(UUID.randomUUID());
        si.setUser(user);

        RouteCheck check = new RouteCheck();
        check.setId(UUID.randomUUID());
        check.setRouteNumber(route);
        check.setCheckedAt(at);
        check.setServiceInfo(si);
        return check;
    }
}
