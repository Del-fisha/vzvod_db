package com.company.vzvod.service;

import com.company.vzvod.dashboard.todayshift.ArticleCountRow;
import com.company.vzvod.dashboard.todayshift.CriminalTypeCountRow;
import com.company.vzvod.dashboard.todayshift.ShiftRouteRow;
import com.company.vzvod.dashboard.todayshift.TodayShiftDashboardSnapshot;
import com.company.vzvod.entity.*;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class TodayShiftDashboardService {

    private final DataManager dataManager;

    public TodayShiftDashboardService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Transactional(readOnly = true)
    public TodayShiftDashboardSnapshot loadSnapshot() {
        return loadSnapshot(LocalDateTime.now(), ZoneId.systemDefault());
    }

    @Transactional(readOnly = true)
    public TodayShiftDashboardSnapshot loadSnapshot(LocalDateTime wallClock, ZoneId zoneId) {
        LocalDate operationalDate = ShiftOperationalDay.resolveOperationalDate(wallClock, zoneId);
        Dep department = DepartmentConverter.departmentFromDate(operationalDate);
        List<Shift> shifts = loadShifts(operationalDate);

        List<ShiftRouteRow> routes = new ArrayList<>();
        int totalIbd = 0;
        int totalStatements = 0;
        int totalClaims = 0;
        int totalAdministrative = 0;
        int totalCriminal = 0;
        Map<ArticleOfAdministrative, Integer> administrativeByArticle = new EnumMap<>(ArticleOfAdministrative.class);
        Map<TypeOfCriminal, Integer> criminalByType = new EnumMap<>(TypeOfCriminal.class);

        for (Shift shift : shifts) {
            routes.add(new ShiftRouteRow(routeLabel(shift), formatRouteEmployees(shift)));
            totalIbd += nullableInt(shift.getIbdWithMigrant());
            totalStatements += nullableInt(shift.getCountOfStatements());
            totalClaims += nullableInt(shift.getCountOfClaims());

            Set<AdministrativeViolation> administrativeViolations = shift.getAdministrativeViolations();
            if (administrativeViolations != null) {
                totalAdministrative += administrativeViolations.size();
                for (AdministrativeViolation violation : administrativeViolations) {
                    ArticleOfAdministrative article = violation.getArticle();
                    if (article != null) {
                        administrativeByArticle.merge(article, 1, Integer::sum);
                    }
                }
            }

            Set<CriminalViolation> criminalViolations = shift.getCriminalViolations();
            if (criminalViolations != null) {
                totalCriminal += criminalViolations.size();
                for (CriminalViolation violation : criminalViolations) {
                    TypeOfCriminal type = violation.getType();
                    if (type != null) {
                        criminalByType.merge(type, 1, Integer::sum);
                    }
                }
            }
        }

        routes.sort(Comparator.comparing(ShiftRouteRow::routeLabel, Comparator.nullsLast(String::compareTo)));

        int departmentNumber = department == null ? 0 : department.getId();

        return new TodayShiftDashboardSnapshot(
                operationalDate,
                departmentNumber,
                List.copyOf(routes),
                totalIbd,
                totalStatements,
                totalClaims,
                totalAdministrative,
                totalCriminal,
                toArticleRows(administrativeByArticle),
                toCriminalRows(criminalByType)
        );
    }

    private List<Shift> loadShifts(LocalDate operationalDate) {
        return dataManager.load(Shift.class)
                .query("select s from Shift s where s.date = :date order by s.number")
                .parameter("date", operationalDate)
                .fetchPlan(f -> f
                        .add("number")
                        .add("countOfStatements")
                        .add("countOfClaims")
                        .add("ibdWithMigrant")
                        .add("units", u -> u.add("user", user -> user
                                .add("lastName")
                                .add("firstName")
                                .add("patronymic")))
                        .add("administrativeViolations", av -> av.add("article"))
                        .add("criminalViolations", cv -> cv.add("type")))
                .list();
    }

    private static String routeLabel(Shift shift) {
        NumberOfShift routeNumber = shift.getNumber();
        return routeNumber == null ? "" : routeNumber.getId();
    }

    private static String formatRouteEmployees(Shift shift) {
        if (shift.getUnits() == null || shift.getUnits().isEmpty()) {
            return "";
        }
        return shift.getUnits().stream()
                .map(ServiceInfo::getUser)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(User::getLastName, Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(User::getFirstName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(User::getShortFio)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private static int nullableInt(Integer value) {
        return value == null ? 0 : value;
    }

    private static List<ArticleCountRow> toArticleRows(Map<ArticleOfAdministrative, Integer> counts) {
        return counts.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().getId()))
                .map(e -> new ArticleCountRow(e.getKey().getId(), e.getValue()))
                .toList();
    }

    private static List<CriminalTypeCountRow> toCriminalRows(Map<TypeOfCriminal, Integer> counts) {
        return counts.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().getId()))
                .map(e -> new CriminalTypeCountRow(e.getKey().getId(), e.getValue()))
                .toList();
    }
}
