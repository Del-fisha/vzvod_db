package com.company.vzvod.service;

import com.company.vzvod.dashboard.todayshift.RouteCheckCheckerOption;
import com.company.vzvod.entity.NumberOfShift;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.entity.TypeOfShift;
import com.company.vzvod.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RouteCheckService: командиры на смене CHECKING")
class RouteCheckServiceCommandersTest {

    @Test
    @DisplayName("собирает уникальных командиров только со смен CHECKING, сортирует по ФИО")
    void collectCommanders_fromCheckingShiftsOnly() {
        ServiceInfo ivanov = unit("Иванов", "И", "О");
        ServiceInfo petrov = unit("Петров", "П", "С");
        ServiceInfo patrol = unit("Сидоров", "С", "И");

        Shift checking = shift(TypeOfShift.CHECKING, ivanov, petrov);
        Shift duplicateChecking = shift(TypeOfShift.CHECKING, ivanov);
        Shift patrolShift = shift(TypeOfShift.VZVOD_ROUTE, patrol);

        List<RouteCheckCheckerOption> options = RouteCheckService.collectCommanders(
                List.of(patrolShift, duplicateChecking, checking)
        );

        assertEquals(2, options.size());
        assertEquals(ivanov.getId(), options.get(0).serviceInfoId());
        assertEquals("Иванов И. О.", options.get(0).label());
        assertEquals(petrov.getId(), options.get(1).serviceInfoId());
        assertEquals("Петров П. С.", options.get(1).label());
    }

    @Test
    @DisplayName("текущий проверяющий остаётся в списке, даже если уже не на смене")
    void withCurrentChecker_keepsMissingCurrent() {
        ServiceInfo onShift = unit("Иванов", "И", "О");
        ServiceInfo former = unit("Тарасов", "А", "Н");
        List<RouteCheckCheckerOption> commanders = RouteCheckService.collectCommanders(
                List.of(shift(TypeOfShift.CHECKING, onShift))
        );

        List<RouteCheckCheckerOption> options = RouteCheckService.withCurrentChecker(
                commanders,
                former.getId(),
                "Тарасов А. Н."
        );

        assertEquals(2, options.size());
        assertEquals(onShift.getId(), options.get(0).serviceInfoId());
        assertEquals(former.getId(), options.get(1).serviceInfoId());
    }

    @Test
    @DisplayName("не дублирует текущего, если он уже среди командиров смены")
    void withCurrentChecker_doesNotDuplicate() {
        ServiceInfo onShift = unit("Иванов", "И", "О");
        List<RouteCheckCheckerOption> commanders = RouteCheckService.collectCommanders(
                List.of(shift(TypeOfShift.CHECKING, onShift))
        );

        List<RouteCheckCheckerOption> options = RouteCheckService.withCurrentChecker(
                commanders,
                onShift.getId(),
                "Иванов И. О."
        );

        assertEquals(1, options.size());
        assertEquals(onShift.getId(), options.get(0).serviceInfoId());
    }

    @Test
    @DisplayName("разрешает только проверяющих из списка")
    void isAllowedChecker_onlyListed() {
        UUID allowed = UUID.randomUUID();
        List<RouteCheckCheckerOption> options = List.of(
                new RouteCheckCheckerOption(allowed, "Иванов И. О.")
        );

        assertTrue(RouteCheckService.isAllowedChecker(allowed, options));
        assertFalse(RouteCheckService.isAllowedChecker(UUID.randomUUID(), options));
        assertFalse(RouteCheckService.isAllowedChecker(null, options));
    }

    private static Shift shift(TypeOfShift type, ServiceInfo... units) {
        Shift s = new Shift();
        s.setId(UUID.randomUUID());
        s.setNumber(type == TypeOfShift.CHECKING ? NumberOfShift.ANOTHER : NumberOfShift._28);
        s.setTypeOfShift(type);
        s.setUnits(new HashSet<>(Set.of(units)));
        return s;
    }

    private static ServiceInfo unit(String last, String first, String patronymic) {
        User user = new User();
        user.setLastName(last);
        user.setFirstName(first);
        user.setPatronymic(patronymic);
        ServiceInfo si = new ServiceInfo();
        si.setId(UUID.randomUUID());
        si.setUser(user);
        return si;
    }
}
