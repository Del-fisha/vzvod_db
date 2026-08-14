package com.company.vzvod.service;

import com.company.vzvod.bot.dto.BotCheckableRoutesResponse;
import com.company.vzvod.bot.dto.BotRouteCheckCreateRequest;
import com.company.vzvod.bot.dto.BotRouteCheckUpdateRequest;
import com.company.vzvod.dashboard.todayshift.RouteChecksRow;
import com.company.vzvod.entity.Dep;
import com.company.vzvod.entity.NumberOfShift;
import com.company.vzvod.entity.RouteCheck;
import com.company.vzvod.entity.RouteCheckFormatter;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.entity.TypeOfShift;
import com.company.vzvod.entity.User;
import io.jmix.core.DataManager;
import io.jmix.core.UnconstrainedDataManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RouteCheckService {

    static final ZoneId MOSCOW = ZoneId.of("Europe/Moscow");

    private final DataManager dataManager;
    private final UnconstrainedDataManager unconstrainedDataManager;

    public RouteCheckService(DataManager dataManager, UnconstrainedDataManager unconstrainedDataManager) {
        this.dataManager = dataManager;
        this.unconstrainedDataManager = unconstrainedDataManager;
    }

    static LocalTime nowCheckedAt(Clock clock) {
        return LocalTime.now(clock).withSecond(0).withNano(0);
    }

    @Transactional(readOnly = true)
    public List<RouteChecksRow> loadRouteChecksRows(LocalDate operationalDate, Dep department) {
        List<Shift> dayShifts = loadDayShifts(dataManager, operationalDate, department);
        List<RouteCheck> checks = loadChecksForDay(dataManager, operationalDate, department);
        return buildRows(dayShifts, checks);
    }

    @Transactional(readOnly = true)
    public BotCheckableRoutesResponse loadCheckableRoutes(UUID userId, UUID checkingShiftId) {
        ServiceInfo si = requireServiceInfo(userId);
        Shift checkingShift = requireAccessibleCheckingShift(checkingShiftId, si.getId());
        List<Shift> dayShifts = loadDayShifts(
                unconstrainedDataManager,
                checkingShift.getDate(),
                checkingShift.getDepartmentToday()
        );
        List<RouteCheck> checks = loadChecksForDay(
                unconstrainedDataManager,
                checkingShift.getDate(),
                checkingShift.getDepartmentToday()
        );
        List<BotCheckableRoutesResponse.BotCheckableRouteItem> routes = new ArrayList<>();
        for (Shift shift : dayShifts) {
            if (shift.getTypeOfShift() == TypeOfShift.CHECKING) {
                continue;
            }
            NumberOfShift route = shift.getNumber();
            if (route == null) {
                continue;
            }
            String routeId = route.getId();
            List<BotCheckableRoutesResponse.BotRouteCheckItem> items = checks.stream()
                    .filter(c -> routeId.equals(c.getRouteNumberId()))
                    .sorted(Comparator.comparing(RouteCheck::getCheckedAt, Comparator.nullsLast(LocalTime::compareTo)))
                    .map(this::toBotItem)
                    .toList();
            routes.add(new BotCheckableRoutesResponse.BotCheckableRouteItem(routeId, routeId, items));
        }
        routes.sort(Comparator.comparing(BotCheckableRoutesResponse.BotCheckableRouteItem::routeLabel,
                Comparator.nullsLast(String::compareTo)));
        return new BotCheckableRoutesResponse(List.copyOf(routes));
    }

    @Transactional
    public BotCheckableRoutesResponse.BotRouteCheckItem recordCheck(
            UUID userId,
            UUID checkingShiftId,
            BotRouteCheckCreateRequest body
    ) {
        if (body == null || body.routeId() == null || body.routeId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "routeId required");
        }
        NumberOfShift route = NumberOfShift.fromId(body.routeId().trim());
        if (route == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid routeId");
        }
        ServiceInfo si = requireServiceInfo(userId);
        Shift checkingShift = requireAccessibleCheckingShift(checkingShiftId, si.getId());
        if (checkingShift.getEndTime() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "shift already closed");
        }
        assertRouteExistsToday(checkingShift, route);

        RouteCheck check = unconstrainedDataManager.create(RouteCheck.class);
        check.setShift(checkingShift);
        check.setServiceInfo(si);
        check.setRouteNumber(route);
        check.setCheckedAt(nowCheckedAt(Clock.system(MOSCOW)));
        RouteCheck saved = unconstrainedDataManager.save(check);
        return toBotItem(reloadCheck(saved.getId()));
    }

    @Transactional
    public BotCheckableRoutesResponse.BotRouteCheckItem updateCheck(
            UUID userId,
            UUID checkId,
            BotRouteCheckUpdateRequest body
    ) {
        if (body == null || body.checkedAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "checkedAt required");
        }
        ServiceInfo si = requireServiceInfo(userId);
        RouteCheck check = loadCheckForUpdate(checkId, si.getId());
        check.setCheckedAt(body.checkedAt().withSecond(0).withNano(0));
        RouteCheck saved = unconstrainedDataManager.save(check);
        return toBotItem(reloadCheck(saved.getId()));
    }

    @Transactional
    public void deleteCheck(UUID userId, UUID checkId) {
        ServiceInfo si = requireServiceInfo(userId);
        RouteCheck check = loadCheckForUpdate(checkId, si.getId());
        unconstrainedDataManager.remove(check);
    }

    @Transactional
    public RouteCheck updateCheckFromUi(UUID checkId, LocalTime checkedAt) {
        if (checkedAt == null) {
            throw new IllegalArgumentException("checkedAt required");
        }
        RouteCheck check = dataManager.load(RouteCheck.class)
                .id(checkId)
                .fetchPlan(f -> f
                        .add("checkedAt")
                        .add("routeNumber")
                        .add("serviceInfo", si -> si.add("user", u -> u
                                .add("lastName").add("firstName").add("patronymic")))
                        .add("shift"))
                .optional()
                .orElseThrow(() -> new IllegalArgumentException("route check not found"));
        check.setCheckedAt(checkedAt.withSecond(0).withNano(0));
        return dataManager.save(check);
    }

    @Transactional
    public void deleteCheckFromUi(UUID checkId) {
        RouteCheck check = dataManager.load(RouteCheck.class)
                .id(checkId)
                .optional()
                .orElseThrow(() -> new IllegalArgumentException("route check not found"));
        dataManager.remove(check);
    }

    static List<RouteChecksRow> buildRows(List<Shift> dayShifts, List<RouteCheck> checks) {
        Map<String, List<RouteCheck>> byRoute = new LinkedHashMap<>();
        for (RouteCheck check : checks) {
            String routeId = check.getRouteNumberId();
            if (routeId == null || routeId.isBlank()) {
                continue;
            }
            byRoute.computeIfAbsent(routeId, k -> new ArrayList<>()).add(check);
        }

        List<RouteChecksRow> rows = new ArrayList<>();
        for (Shift shift : dayShifts) {
            if (shift.getTypeOfShift() == TypeOfShift.CHECKING) {
                continue;
            }
            NumberOfShift route = shift.getNumber();
            if (route == null) {
                continue;
            }
            String routeId = route.getId();
            List<RouteCheck> routeChecks = byRoute.getOrDefault(routeId, List.of());
            List<RouteChecksRow.RouteCheckEntry> entries = routeChecks.stream()
                    .sorted(Comparator.comparing(RouteCheck::getCheckedAt, Comparator.nullsLast(LocalTime::compareTo)))
                    .map(RouteCheckService::toDashboardEntry)
                    .toList();
            rows.add(new RouteChecksRow(routeId, entries));
        }
        rows.sort(Comparator.comparing(RouteChecksRow::routeLabel, Comparator.nullsLast(String::compareTo)));
        return List.copyOf(rows);
    }

    private static RouteChecksRow.RouteCheckEntry toDashboardEntry(RouteCheck check) {
        String fio = checkerShortFio(check.getServiceInfo());
        String display = RouteCheckFormatter.formatEntry(check.getCheckedAt(), fio);
        return new RouteChecksRow.RouteCheckEntry(check.getId(), check.getCheckedAt(), fio, display);
    }

    private BotCheckableRoutesResponse.BotRouteCheckItem toBotItem(RouteCheck check) {
        String fio = checkerShortFio(check.getServiceInfo());
        String display = RouteCheckFormatter.formatEntry(check.getCheckedAt(), fio);
        return new BotCheckableRoutesResponse.BotRouteCheckItem(
                check.getId(),
                check.getCheckedAt(),
                fio == null || fio.isBlank() ? null : fio,
                display
        );
    }

    private static String checkerShortFio(ServiceInfo serviceInfo) {
        if (serviceInfo == null || serviceInfo.getUser() == null) {
            return "";
        }
        return serviceInfo.getUser().getShortFio();
    }

    private void assertRouteExistsToday(Shift checkingShift, NumberOfShift route) {
        List<Shift> dayShifts = loadDayShifts(
                unconstrainedDataManager,
                checkingShift.getDate(),
                checkingShift.getDepartmentToday()
        );
        boolean exists = dayShifts.stream()
                .anyMatch(s -> s.getTypeOfShift() != TypeOfShift.CHECKING && route.equals(s.getNumber()));
        if (!exists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "route not on today's shift");
        }
    }

    private Shift requireAccessibleCheckingShift(UUID shiftId, UUID serviceInfoId) {
        Shift shift = unconstrainedDataManager.load(Shift.class)
                .id(shiftId)
                .fetchPlan(f -> f
                        .add("id")
                        .add("date")
                        .add("departmentToday")
                        .add("typeOfShift")
                        .add("endTime")
                        .add("units", u -> u.add("id")))
                .optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "shift not found"));
        boolean member = shift.getUnits() != null && shift.getUnits().stream()
                .anyMatch(u -> u != null && serviceInfoId.equals(u.getId()));
        if (!member) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "shift not accessible");
        }
        if (shift.getTypeOfShift() != TypeOfShift.CHECKING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "shift is not CHECKING");
        }
        return shift;
    }

    private RouteCheck loadCheckForUpdate(UUID checkId, UUID serviceInfoId) {
        RouteCheck check = unconstrainedDataManager.load(RouteCheck.class)
                .id(checkId)
                .fetchPlan(f -> f
                        .add("checkedAt")
                        .add("routeNumber")
                        .add("serviceInfo", si -> si.add("id").add("user", u -> u
                                .add("lastName").add("firstName").add("patronymic")))
                        .add("shift", s -> s
                                .add("id")
                                .add("endTime")
                                .add("typeOfShift")
                                .add("units", u -> u.add("id"))))
                .optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "route check not found"));
        Shift shift = check.getShift();
        boolean member = shift != null && shift.getUnits() != null && shift.getUnits().stream()
                .anyMatch(u -> u != null && serviceInfoId.equals(u.getId()));
        if (!member) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "route check not accessible");
        }
        return check;
    }

    private RouteCheck reloadCheck(UUID id) {
        return unconstrainedDataManager.load(RouteCheck.class)
                .id(id)
                .fetchPlan(f -> f
                        .add("checkedAt")
                        .add("routeNumber")
                        .add("serviceInfo", si -> si.add("user", u -> u
                                .add("lastName").add("firstName").add("patronymic"))))
                .one();
    }

    private ServiceInfo requireServiceInfo(UUID userId) {
        User user = unconstrainedDataManager.load(User.class)
                .id(userId)
                .fetchPlan(f -> f.add("serviceInfo", si -> si.add("id")))
                .optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        ServiceInfo si = user.getServiceInfo();
        if (si == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "service info missing");
        }
        return si;
    }

    private static List<Shift> loadDayShifts(
            io.jmix.core.UnconstrainedDataManager manager,
            LocalDate date,
            Dep department
    ) {
        List<Shift> all = manager.load(Shift.class)
                .query("select s from Shift s where s.date = :date order by s.number")
                .parameter("date", date)
                .fetchPlan(f -> f
                        .add("number")
                        .add("typeOfShift")
                        .add("departmentToday"))
                .list();
        return filterByDepartment(all, department);
    }

    private static List<Shift> loadDayShifts(DataManager manager, LocalDate date, Dep department) {
        List<Shift> all = manager.load(Shift.class)
                .query("select s from Shift s where s.date = :date order by s.number")
                .parameter("date", date)
                .fetchPlan(f -> f
                        .add("number")
                        .add("typeOfShift")
                        .add("departmentToday"))
                .list();
        return filterByDepartment(all, department);
    }

    private static List<RouteCheck> loadChecksForDay(
            UnconstrainedDataManager manager,
            LocalDate date,
            Dep department
    ) {
        List<RouteCheck> all = manager.load(RouteCheck.class)
                .query("select c from RouteCheck c join c.shift s where s.date = :date")
                .parameter("date", date)
                .fetchPlan(f -> f
                        .add("checkedAt")
                        .add("routeNumber")
                        .add("shift", s -> s.add("departmentToday").add("date"))
                        .add("serviceInfo", si -> si.add("user", u -> u
                                .add("lastName").add("firstName").add("patronymic"))))
                .list();
        if (department == null) {
            return all;
        }
        return all.stream()
                .filter(c -> c.getShift() != null && department.equals(c.getShift().getDepartmentToday()))
                .toList();
    }

    private static List<RouteCheck> loadChecksForDay(DataManager manager, LocalDate date, Dep department) {
        List<RouteCheck> all = manager.load(RouteCheck.class)
                .query("select c from RouteCheck c join c.shift s where s.date = :date")
                .parameter("date", date)
                .fetchPlan(f -> f
                        .add("checkedAt")
                        .add("routeNumber")
                        .add("shift", s -> s.add("departmentToday").add("date"))
                        .add("serviceInfo", si -> si.add("user", u -> u
                                .add("lastName").add("firstName").add("patronymic"))))
                .list();
        if (department == null) {
            return all;
        }
        return all.stream()
                .filter(c -> c.getShift() != null && department.equals(c.getShift().getDepartmentToday()))
                .toList();
    }

    private static List<Shift> filterByDepartment(List<Shift> all, Dep department) {
        if (department == null) {
            return all;
        }
        return all.stream()
                .filter(s -> department.equals(s.getDepartmentToday()))
                .toList();
    }
}
