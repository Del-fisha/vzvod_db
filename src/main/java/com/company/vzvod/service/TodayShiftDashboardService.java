package com.company.vzvod.service;

import com.company.vzvod.bot.dto.BotLabeledCount;
import com.company.vzvod.bot.dto.BotTodayDashboardResponse;
import com.company.vzvod.bot.dto.BotTodayRouteRow;
import com.company.vzvod.bot.dto.BotTodayTotals;
import com.company.vzvod.dashboard.todayshift.ArticleCountRow;
import com.company.vzvod.dashboard.todayshift.CriminalTypeCountRow;
import com.company.vzvod.dashboard.todayshift.PeriodMetricRow;
import com.company.vzvod.dashboard.todayshift.RouteDetailsRow;
import com.company.vzvod.dashboard.todayshift.ShiftRouteRow;
import com.company.vzvod.dashboard.todayshift.TodayShiftDashboardSnapshot;
import com.company.vzvod.entity.*;
import com.company.vzvod.util.EmployeeOrdering;
import io.jmix.core.DataManager;
import io.jmix.core.UnconstrainedDataManager;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class TodayShiftDashboardService {

    private static final Set<ArticleOfAdministrative> AP_OTHER_ARTICLES = Set.of(
            ArticleOfAdministrative._11_15,
            ArticleOfAdministrative._19_3,
            ArticleOfAdministrative._20_25,
            ArticleOfAdministrative.ANOTHER
    );

    private static final Set<ArticleOfAdministrative> AP_201_ARTICLES = Set.of(
            ArticleOfAdministrative._20_1,
            ArticleOfAdministrative._20_1_2
    );

    /** Статьи для строки «Итого» в блоке административных показателей. */
    private static final Set<ArticleOfAdministrative> PERIOD_ADMIN_SUM_ARTICLES = Set.of(
            ArticleOfAdministrative._18_8,
            ArticleOfAdministrative._19_3,
            ArticleOfAdministrative._20_1,
            ArticleOfAdministrative._20_1_2,
            ArticleOfAdministrative._20_20,
            ArticleOfAdministrative._20_21,
            ArticleOfAdministrative._11_15,
            ArticleOfAdministrative._20_25,
            ArticleOfAdministrative.ANOTHER
    );

    private static final Locale RU = Locale.forLanguageTag("ru");

    private final DataManager dataManager;
    private final UnconstrainedDataManager unconstrainedDataManager;
    private final MessageSource messageSource;

    public TodayShiftDashboardService(
            DataManager dataManager,
            UnconstrainedDataManager unconstrainedDataManager,
            MessageSource messageSource
    ) {
        this.dataManager = dataManager;
        this.unconstrainedDataManager = unconstrainedDataManager;
        this.messageSource = messageSource;
    }

    @Transactional(readOnly = true)
    public TodayShiftDashboardSnapshot loadSnapshot() {
        return loadSnapshot(LocalDateTime.now(), ZoneId.systemDefault());
    }

    @Transactional(readOnly = true)
    public TodayShiftDashboardSnapshot loadSnapshot(LocalDateTime wallClock, ZoneId zoneId) {
        LocalDate operationalDate = ShiftOperationalDay.resolveOperationalDate(wallClock, zoneId);
        Dep department = DepartmentConverter.departmentFromDate(operationalDate);
        return loadSnapshot(operationalDate, department);
    }

    /**
     * Дашборд за конкретный операционный день и отделение (исторические сутки из AllTodayShifts).
     */
    @Transactional(readOnly = true)
    public TodayShiftDashboardSnapshot loadSnapshot(LocalDate operationalDate, Dep department) {
        List<Shift> shifts = loadShifts(operationalDate, department);

        List<ShiftRouteRow> routes = new ArrayList<>();
        List<RouteDetailsRow> routeDetails = new ArrayList<>();
        int totalIbd = 0;
        int totalMigrant = 0;
        int totalStatements = 0;
        int totalClaims = 0;
        int totalAdministrative = 0;
        int totalCriminal = 0;
        Map<ArticleOfAdministrative, Integer> administrativeByArticle = new EnumMap<>(ArticleOfAdministrative.class);
        Map<TypeOfCriminal, Integer> criminalByType = new EnumMap<>(TypeOfCriminal.class);

        for (Shift shift : shifts) {
            routes.add(new ShiftRouteRow(routeLabel(shift), formatRouteEmployees(shift), shift.getEndTime()));
            totalIbd += nullableInt(shift.getIbdr());
            totalMigrant += nullableInt(shift.getMigrant());
            totalStatements += nullableInt(shift.getCountOfStatements());
            totalClaims += nullableInt(shift.getCountOfClaims());

            aggregateViolations(shift, administrativeByArticle, criminalByType);
            totalAdministrative += countAdministrative(shift);
            totalCriminal += countCriminal(shift);

            if (shift.getTypeOfShift() != TypeOfShift.CHECKING) {
                routeDetails.add(buildRouteDetailsRow(shift));
            }
        }

        routes.sort(Comparator.comparing(ShiftRouteRow::routeLabel, Comparator.nullsLast(String::compareTo)));
        routeDetails.sort(Comparator.comparing(RouteDetailsRow::routeLabel, Comparator.nullsLast(String::compareTo)));

        int departmentNumber = department == null ? 0 : department.getId();
        List<PeriodMetricRow> periodMetrics = buildPeriodMetrics(operationalDate, department);

        return new TodayShiftDashboardSnapshot(
                operationalDate,
                departmentNumber,
                List.copyOf(routes),
                totalIbd,
                totalMigrant,
                totalStatements,
                totalClaims,
                totalAdministrative,
                totalCriminal,
                toArticleRows(administrativeByArticle),
                toCriminalRows(criminalByType),
                List.copyOf(routeDetails),
                periodMetrics
        );
    }

    @Transactional(readOnly = true)
    public BotTodayDashboardResponse loadBotDashboard() {
        return loadBotDashboard(LocalDateTime.now(), ZoneId.systemDefault());
    }

    @Transactional(readOnly = true)
    public BotTodayDashboardResponse loadBotDashboard(LocalDateTime wallClock, ZoneId zoneId) {
        LocalDate operationalDate = ShiftOperationalDay.resolveOperationalDate(wallClock, zoneId);
        Dep department = DepartmentConverter.departmentFromDate(operationalDate);
        return loadBotDashboard(operationalDate, department);
    }

    /**
     * Сводка операционных суток для мобильного API: все маршруты (в т.ч. проверочные)
     * с полными показателями и подписанной разбивкой по статьям/типам преступлений.
     */
    @Transactional(readOnly = true)
    public BotTodayDashboardResponse loadBotDashboard(LocalDate operationalDate, Dep department) {
        // Мобильный/бот API не устанавливает Spring Security Authentication (доступ проверяется в контроллерах
        // по X-Api-Key/X-Mobile-Token), поэтому используем UnconstrainedDataManager вместо обычного DataManager,
        // который требует аутентифицированного пользователя для проверки прав доступа.
        List<Shift> shifts = loadShiftsUnconstrained(operationalDate, department);

        List<BotTodayRouteRow> routes = new ArrayList<>();
        int totalIbdr = 0;
        int totalMigrant = 0;
        int totalStatements = 0;
        int totalClaims = 0;
        int totalAdministrative = 0;
        int totalCriminal = 0;
        Map<ArticleOfAdministrative, Integer> administrativeByArticle = new EnumMap<>(ArticleOfAdministrative.class);
        Map<TypeOfCriminal, Integer> criminalByType = new EnumMap<>(TypeOfCriminal.class);

        for (Shift shift : shifts) {
            Map<ArticleOfAdministrative, Integer> shiftAdminByArticle = new EnumMap<>(ArticleOfAdministrative.class);
            Map<TypeOfCriminal, Integer> shiftCriminalByType = new EnumMap<>(TypeOfCriminal.class);
            aggregateViolations(shift, shiftAdminByArticle, shiftCriminalByType);
            shiftAdminByArticle.forEach((article, count) -> administrativeByArticle.merge(article, count, Integer::sum));
            shiftCriminalByType.forEach((type, count) -> criminalByType.merge(type, count, Integer::sum));

            int shiftAdministrative = countAdministrative(shift);
            int shiftCriminal = countCriminal(shift);
            totalAdministrative += shiftAdministrative;
            totalCriminal += shiftCriminal;
            totalIbdr += nullableInt(shift.getIbdr());
            totalMigrant += nullableInt(shift.getMigrant());
            totalStatements += nullableInt(shift.getCountOfStatements());
            totalClaims += nullableInt(shift.getCountOfClaims());

            routes.add(new BotTodayRouteRow(
                    routeLabel(shift),
                    employeesList(shift),
                    nullableInt(shift.getIbdr()),
                    nullableInt(shift.getMigrant()),
                    nullableInt(shift.getCountOfStatements()),
                    nullableInt(shift.getCountOfClaims()),
                    shiftAdministrative,
                    shiftCriminal,
                    toLabeledArticleRows(shiftAdminByArticle),
                    toLabeledCriminalRows(shiftCriminalByType)
            ));
        }

        routes.sort(Comparator.comparing(BotTodayRouteRow::routeLabel, Comparator.nullsLast(String::compareTo)));

        int departmentNumber = department == null ? 0 : department.getId();
        BotTodayTotals totals = new BotTodayTotals(
                totalIbdr,
                totalMigrant,
                totalStatements,
                totalClaims,
                totalAdministrative,
                totalCriminal,
                toLabeledArticleRows(administrativeByArticle),
                toLabeledCriminalRows(criminalByType)
        );

        return new BotTodayDashboardResponse(operationalDate, departmentNumber, List.copyOf(routes), totals);
    }

    private static List<String> employeesList(Shift shift) {
        if (shift.getUnits() == null || shift.getUnits().isEmpty()) {
            return List.of();
        }
        return shift.getUnits().stream()
                .sorted(EmployeeOrdering.serviceInfoComparator())
                .map(ServiceInfo::getUser)
                .filter(Objects::nonNull)
                .map(User::getShortFio)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .toList();
    }

    private static List<BotLabeledCount> toLabeledArticleRows(Map<ArticleOfAdministrative, Integer> counts) {
        return counts.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().getId()))
                .map(e -> new BotLabeledCount(formatAdministrativeArticleLabel(e.getKey()), e.getValue()))
                .toList();
    }

    private List<BotLabeledCount> toLabeledCriminalRows(Map<TypeOfCriminal, Integer> counts) {
        return counts.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().getId()))
                .map(e -> new BotLabeledCount(criminalTypeLabel(e.getKey()), e.getValue()))
                .toList();
    }

    /**
     * Подпись статьи КоАП для мобильного API: {@code _20_20} → {@code "20.20"}, {@code ANOTHER} → {@code "иные"}.
     */
    public static String formatAdministrativeArticleLabel(ArticleOfAdministrative article) {
        if (article == null) {
            return null;
        }
        if (article == ArticleOfAdministrative.ANOTHER) {
            return "иные";
        }
        String name = article.name();
        String stripped = name.startsWith("_") ? name.substring(1) : name;
        return stripped.replace('_', '.');
    }

    private String criminalTypeLabel(TypeOfCriminal type) {
        if (type == null) {
            return null;
        }
        return enumMessage("TypeOfCriminal", type.name());
    }

    private String enumMessage(String enumSimpleName, String enumConstantName) {
        String code = "com.company.vzvod.entity/" + enumSimpleName + "." + enumConstantName;
        try {
            return messageSource.getMessage(code, null, RU);
        } catch (NoSuchMessageException e) {
            return enumConstantName;
        }
    }

    private static RouteDetailsRow buildRouteDetailsRow(Shift shift) {
        return new RouteDetailsRow(
                routeLabel(shift),
                countAdminArticles(shift, Set.of(ArticleOfAdministrative._18_8)),
                countAdminArticles(shift, AP_201_ARTICLES),
                countAdminArticles(shift, Set.of(ArticleOfAdministrative._20_20)),
                countAdminArticles(shift, Set.of(ArticleOfAdministrative._20_21)),
                countAdminArticles(shift, AP_OTHER_ARTICLES),
                countCriminalType(shift, TypeOfCriminal.FEDERAL_WANTED),
                countCriminalType(shift, TypeOfCriminal.WATCH_LIST),
                countCriminalType(shift, TypeOfCriminal.LOCAL_SEARCH),
                countCriminalType(shift, TypeOfCriminal.IDENTIFICATION),
                countCriminalType(shift, TypeOfCriminal.HOT_PURSUIT),
                nullableInt(shift.getIbdr()),
                nullableInt(shift.getCountOfStatements())
        );
    }

    private static void aggregateViolations(
            Shift shift,
            Map<ArticleOfAdministrative, Integer> administrativeByArticle,
            Map<TypeOfCriminal, Integer> criminalByType
    ) {
        Set<AdministrativeViolation> administrativeViolations = shift.getAdministrativeViolations();
        if (administrativeViolations != null) {
            for (AdministrativeViolation violation : administrativeViolations) {
                ArticleOfAdministrative article = violation.getArticle();
                if (article != null) {
                    administrativeByArticle.merge(article, 1, Integer::sum);
                }
            }
        }

        Set<CriminalViolation> criminalViolations = shift.getCriminalViolations();
        if (criminalViolations != null) {
            for (CriminalViolation violation : criminalViolations) {
                TypeOfCriminal type = violation.getType();
                if (type != null) {
                    criminalByType.merge(type, 1, Integer::sum);
                }
            }
        }
    }

    private List<PeriodMetricRow> buildPeriodMetrics(LocalDate operationalDate, Dep department) {
        LocalDate monthStart = operationalDate.withDayOfMonth(1);
        LocalDate monthEnd = operationalDate.withDayOfMonth(operationalDate.lengthOfMonth());
        LocalDate yearStart = operationalDate.withDayOfYear(1);
        LocalDate yearEnd = operationalDate.withMonth(12).withDayOfMonth(31);
        List<Shift> periodShifts = loadPeriodShifts(yearStart, yearEnd);

        List<PeriodMetricRow> rows = new ArrayList<>();
        rows.add(metricRow("period.hotPursuit", false, department, operationalDate, operationalDate,
                monthStart, monthEnd, yearStart, yearEnd, periodShifts, PeriodCountKind.CRIMINAL_TYPE, TypeOfCriminal.HOT_PURSUIT));
        rows.add(metricRow("period.federalWanted", false, department, operationalDate, operationalDate,
                monthStart, monthEnd, yearStart, yearEnd, periodShifts, PeriodCountKind.CRIMINAL_TYPE, TypeOfCriminal.FEDERAL_WANTED));
        rows.add(metricRow("period.localSearch", false, department, operationalDate, operationalDate,
                monthStart, monthEnd, yearStart, yearEnd, periodShifts, PeriodCountKind.CRIMINAL_TYPE, TypeOfCriminal.LOCAL_SEARCH));
        rows.add(metricRow("period.watchList", false, department, operationalDate, operationalDate,
                monthStart, monthEnd, yearStart, yearEnd, periodShifts, PeriodCountKind.CRIMINAL_TYPE, TypeOfCriminal.WATCH_LIST));
        rows.add(metricRowShiftScalar("period.statements", department, operationalDate, operationalDate,
                monthStart, monthEnd, yearStart, yearEnd, periodShifts, PeriodCountKind.STATEMENTS));
        rows.add(metricRowShiftScalar("period.ibdr", department, operationalDate, operationalDate,
                monthStart, monthEnd, yearStart, yearEnd, periodShifts, PeriodCountKind.IBDR));

        rows.add(sectionHeader("period.adminSection"));
        rows.add(metricRowAdmin("period.art188", department, operationalDate, operationalDate,
                monthStart, monthEnd, yearStart, yearEnd, periodShifts, ArticleOfAdministrative._18_8));
        rows.add(metricRowAdmin("period.art193", department, operationalDate, operationalDate,
                monthStart, monthEnd, yearStart, yearEnd, periodShifts, ArticleOfAdministrative._19_3));
        rows.add(metricRowAdminArticles("period.art201", department, operationalDate, operationalDate,
                monthStart, monthEnd, yearStart, yearEnd, periodShifts, AP_201_ARTICLES));
        rows.add(metricRowAdmin("period.art2020", department, operationalDate, operationalDate,
                monthStart, monthEnd, yearStart, yearEnd, periodShifts, ArticleOfAdministrative._20_20));
        rows.add(metricRowAdmin("period.art2021", department, operationalDate, operationalDate,
                monthStart, monthEnd, yearStart, yearEnd, periodShifts, ArticleOfAdministrative._20_21));
        rows.add(metricRowAdmin("period.art1115", department, operationalDate, operationalDate,
                monthStart, monthEnd, yearStart, yearEnd, periodShifts, ArticleOfAdministrative._11_15));
        rows.add(metricRowAdmin("period.art2025", department, operationalDate, operationalDate,
                monthStart, monthEnd, yearStart, yearEnd, periodShifts, ArticleOfAdministrative._20_25));
        rows.add(metricRowAdmin("period.artOther", department, operationalDate, operationalDate,
                monthStart, monthEnd, yearStart, yearEnd, periodShifts, ArticleOfAdministrative.ANOTHER));
        rows.add(metricRowAdminArticlesSum("period.adminSum", department, operationalDate, operationalDate,
                monthStart, monthEnd, yearStart, yearEnd, periodShifts, PERIOD_ADMIN_SUM_ARTICLES));
        rows.add(metricRowShiftScalar("period.minors", department, operationalDate, operationalDate,
                monthStart, monthEnd, yearStart, yearEnd, periodShifts, PeriodCountKind.MINORS));

        rows.add(sectionHeader("period.disciplineSection"));
        rows.add(metricRowShiftScalar("period.claims", department, operationalDate, operationalDate,
                monthStart, monthEnd, yearStart, yearEnd, periodShifts, PeriodCountKind.CLAIMS));
        rows.add(metricRowImpact("period.weapon", department, operationalDate, operationalDate,
                monthStart, monthEnd, yearStart, yearEnd, periodShifts, Impact.WEAPON));
        rows.add(metricRowImpact("period.specialTools", department, operationalDate, operationalDate,
                monthStart, monthEnd, yearStart, yearEnd, periodShifts, Impact.SPECIAL_TOOLS));
        rows.add(metricRowImpact("period.physicalForce", department, operationalDate, operationalDate,
                monthStart, monthEnd, yearStart, yearEnd, periodShifts, Impact.PHYSICAL_FORCE));

        return List.copyOf(rows);
    }

    private PeriodMetricRow sectionHeader(String messageKey) {
        return new PeriodMetricRow(messageKey, true, false, 0, 0, 0, 0);
    }

    private PeriodMetricRow metricRow(
            String messageKey,
            boolean sectionHeader,
            Dep department,
            LocalDate shiftFrom,
            LocalDate shiftTo,
            LocalDate monthFrom,
            LocalDate monthTo,
            LocalDate yearFrom,
            LocalDate yearTo,
            List<Shift> periodShifts,
            PeriodCountKind kind,
            TypeOfCriminal criminalType
    ) {
        return metricRow(messageKey, sectionHeader, department, shiftFrom, shiftTo, monthFrom, monthTo, yearFrom, yearTo,
                periodShifts, kind, criminalType, null, null);
    }

    private PeriodMetricRow metricRow(
            String messageKey,
            boolean sectionHeader,
            Dep department,
            LocalDate shiftFrom,
            LocalDate shiftTo,
            LocalDate monthFrom,
            LocalDate monthTo,
            LocalDate yearFrom,
            LocalDate yearTo,
            List<Shift> periodShifts,
            PeriodCountKind kind,
            TypeOfCriminal criminalType,
            ArticleOfAdministrative article,
            Impact impact
    ) {
        int shift = countForPeriod(periodShifts, department, shiftFrom, shiftTo, kind, criminalType, article, null, impact);
        int month = countForPeriod(periodShifts, department, monthFrom, monthTo, kind, criminalType, article, null, impact);
        int year = countForPeriod(periodShifts, department, yearFrom, yearTo, kind, criminalType, article, null, impact);
        int total = countForPeriod(periodShifts, Dep.FIRST, yearFrom, yearTo, kind, criminalType, article, null, impact)
                + countForPeriod(periodShifts, Dep.SECOND, yearFrom, yearTo, kind, criminalType, article, null, impact);
        return new PeriodMetricRow(messageKey, sectionHeader, false, shift, month, year, total);
    }

    private PeriodMetricRow metricRowAdminArticles(
            String messageKey,
            Dep department,
            LocalDate shiftFrom,
            LocalDate shiftTo,
            LocalDate monthFrom,
            LocalDate monthTo,
            LocalDate yearFrom,
            LocalDate yearTo,
            List<Shift> periodShifts,
            Set<ArticleOfAdministrative> articles
    ) {
        int shift = countForPeriod(periodShifts, department, shiftFrom, shiftTo, PeriodCountKind.ADMIN_ARTICLES, null, null, articles, null);
        int month = countForPeriod(periodShifts, department, monthFrom, monthTo, PeriodCountKind.ADMIN_ARTICLES, null, null, articles, null);
        int year = countForPeriod(periodShifts, department, yearFrom, yearTo, PeriodCountKind.ADMIN_ARTICLES, null, null, articles, null);
        int total = countForPeriod(periodShifts, Dep.FIRST, yearFrom, yearTo, PeriodCountKind.ADMIN_ARTICLES, null, null, articles, null)
                + countForPeriod(periodShifts, Dep.SECOND, yearFrom, yearTo, PeriodCountKind.ADMIN_ARTICLES, null, null, articles, null);
        return new PeriodMetricRow(messageKey, false, false, shift, month, year, total);
    }

    private PeriodMetricRow metricRowAdminArticlesSum(
            String messageKey,
            Dep department,
            LocalDate shiftFrom,
            LocalDate shiftTo,
            LocalDate monthFrom,
            LocalDate monthTo,
            LocalDate yearFrom,
            LocalDate yearTo,
            List<Shift> periodShifts,
            Set<ArticleOfAdministrative> articles
    ) {
        int shift = countForPeriod(periodShifts, department, shiftFrom, shiftTo, PeriodCountKind.ADMIN_ARTICLES, null, null, articles, null);
        int month = countForPeriod(periodShifts, department, monthFrom, monthTo, PeriodCountKind.ADMIN_ARTICLES, null, null, articles, null);
        int year = countForPeriod(periodShifts, department, yearFrom, yearTo, PeriodCountKind.ADMIN_ARTICLES, null, null, articles, null);
        int total = countForPeriod(periodShifts, Dep.FIRST, yearFrom, yearTo, PeriodCountKind.ADMIN_ARTICLES, null, null, articles, null)
                + countForPeriod(periodShifts, Dep.SECOND, yearFrom, yearTo, PeriodCountKind.ADMIN_ARTICLES, null, null, articles, null);
        return new PeriodMetricRow(messageKey, false, true, shift, month, year, total);
    }

    private PeriodMetricRow metricRowAdmin(
            String messageKey,
            Dep department,
            LocalDate shiftFrom,
            LocalDate shiftTo,
            LocalDate monthFrom,
            LocalDate monthTo,
            LocalDate yearFrom,
            LocalDate yearTo,
            List<Shift> periodShifts,
            ArticleOfAdministrative article
    ) {
        return metricRow(messageKey, false, department, shiftFrom, shiftTo, monthFrom, monthTo, yearFrom, yearTo,
                periodShifts, PeriodCountKind.ADMIN_ARTICLE, null, article, null);
    }

    private PeriodMetricRow metricRowShiftScalar(
            String messageKey,
            Dep department,
            LocalDate shiftFrom,
            LocalDate shiftTo,
            LocalDate monthFrom,
            LocalDate monthTo,
            LocalDate yearFrom,
            LocalDate yearTo,
            List<Shift> periodShifts,
            PeriodCountKind kind
    ) {
        return metricRow(messageKey, false, department, shiftFrom, shiftTo, monthFrom, monthTo, yearFrom, yearTo,
                periodShifts, kind, null, null, null);
    }

    private PeriodMetricRow metricRowImpact(
            String messageKey,
            Dep department,
            LocalDate shiftFrom,
            LocalDate shiftTo,
            LocalDate monthFrom,
            LocalDate monthTo,
            LocalDate yearFrom,
            LocalDate yearTo,
            List<Shift> periodShifts,
            Impact impact
    ) {
        return metricRow(messageKey, false, department, shiftFrom, shiftTo, monthFrom, monthTo, yearFrom, yearTo,
                periodShifts, PeriodCountKind.IMPACT, null, null, impact);
    }

    private int countForPeriod(
            List<Shift> periodShifts,
            Dep department,
            LocalDate from,
            LocalDate to,
            PeriodCountKind kind,
            TypeOfCriminal criminalType,
            ArticleOfAdministrative article,
            Set<ArticleOfAdministrative> articles,
            Impact impact
    ) {
        if (department == null) {
            return 0;
        }
        return switch (kind) {
            case CRIMINAL_TYPE -> countCriminalViolations(periodShifts, department, from, to, criminalType);
            case ADMIN_ARTICLE -> countAdministrativeViolations(periodShifts, department, from, to, Set.of(article));
            case ADMIN_ARTICLES -> countAdministrativeViolations(periodShifts, department, from, to, articles);
            case STATEMENTS -> sumShiftField(periodShifts, department, from, to, ShiftScalarField.STATEMENTS);
            case IBDR -> sumShiftField(periodShifts, department, from, to, ShiftScalarField.IBDR);
            case CLAIMS -> sumShiftField(periodShifts, department, from, to, ShiftScalarField.CLAIMS);
            case IMPACT -> countImpactViolations(periodShifts, department, from, to, impact);
            case MINORS -> 0;
        };
    }

    private List<Shift> loadPeriodShifts(LocalDate from, LocalDate to) {
        return dataManager.load(Shift.class)
                .query("select s from Shift s where s.date >= :from and s.date <= :to")
                .parameter("from", from)
                .parameter("to", to)
                .fetchPlan(f -> f
                        .add("date")
                        .add("departmentToday")
                        .add("countOfStatements")
                        .add("countOfClaims")
                        .add("ibdr")
                        .add("administrativeViolations", av -> av.add("article").add("impact"))
                        .add("criminalViolations", cv -> cv.add("type").add("impact")))
                .list();
    }

    private static boolean matchesShift(Shift shift, Dep department, LocalDate from, LocalDate to) {
        if (shift == null || shift.getDate() == null) {
            return false;
        }
        if (!department.equals(shift.getDepartmentToday())) {
            return false;
        }
        return !shift.getDate().isBefore(from) && !shift.getDate().isAfter(to);
    }

    private static int countCriminalViolations(
            List<Shift> shifts,
            Dep department,
            LocalDate from,
            LocalDate to,
            TypeOfCriminal type
    ) {
        int count = 0;
        for (Shift shift : shifts) {
            if (!matchesShift(shift, department, from, to)) {
                continue;
            }
            count += countCriminalType(shift, type);
        }
        return count;
    }

    private static int countAdministrativeViolations(
            List<Shift> shifts,
            Dep department,
            LocalDate from,
            LocalDate to,
            Set<ArticleOfAdministrative> articles
    ) {
        int count = 0;
        for (Shift shift : shifts) {
            if (!matchesShift(shift, department, from, to)) {
                continue;
            }
            count += countAdminArticles(shift, articles);
        }
        return count;
    }

    private static int countImpactViolations(
            List<Shift> shifts,
            Dep department,
            LocalDate from,
            LocalDate to,
            Impact impact
    ) {
        int count = 0;
        for (Shift shift : shifts) {
            if (!matchesShift(shift, department, from, to)) {
                continue;
            }
            Set<AdministrativeViolation> administrative = shift.getAdministrativeViolations();
            if (administrative != null) {
                for (AdministrativeViolation violation : administrative) {
                    if (impact.equals(violation.getImpact())) {
                        count++;
                    }
                }
            }
            Set<CriminalViolation> criminal = shift.getCriminalViolations();
            if (criminal != null) {
                for (CriminalViolation violation : criminal) {
                    if (impact.equals(violation.getImpact())) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static int sumShiftField(
            List<Shift> shifts,
            Dep department,
            LocalDate from,
            LocalDate to,
            ShiftScalarField field
    ) {
        int sum = 0;
        for (Shift shift : shifts) {
            if (!matchesShift(shift, department, from, to)) {
                continue;
            }
            sum += switch (field) {
                case STATEMENTS -> nullableInt(shift.getCountOfStatements());
                case IBDR -> nullableInt(shift.getIbdr());
                case CLAIMS -> nullableInt(shift.getCountOfClaims());
            };
        }
        return sum;
    }

    private List<Shift> loadShifts(LocalDate operationalDate, Dep department) {
        var loader = dataManager.load(Shift.class)
                .query("select s from Shift s where s.date = :date order by s.number")
                .parameter("date", operationalDate)
                .fetchPlan(f -> f
                        .add("number")
                        .add("typeOfShift")
                        .add("departmentToday")
                        .add("countOfStatements")
                        .add("countOfClaims")
                        .add("ibdr")
                        .add("migrant")
                        .add("endTime")
                        .add("units", u -> u
                                .add("post")
                                .add("rank")
                                .add("user", user -> user
                                        .add("lastName")
                                        .add("firstName")
                                        .add("patronymic")))
                        .add("administrativeViolations", av -> av.add("article"))
                        .add("criminalViolations", cv -> cv.add("type")));

        List<Shift> all = loader.list();
        if (department == null) {
            return all;
        }
        return all.stream()
                .filter(s -> department.equals(s.getDepartmentToday()))
                .toList();
    }

    private List<Shift> loadShiftsUnconstrained(LocalDate operationalDate, Dep department) {
        var loader = unconstrainedDataManager.load(Shift.class)
                .query("select s from Shift s where s.date = :date order by s.number")
                .parameter("date", operationalDate)
                .fetchPlan(f -> f
                        .add("number")
                        .add("typeOfShift")
                        .add("departmentToday")
                        .add("countOfStatements")
                        .add("countOfClaims")
                        .add("ibdr")
                        .add("migrant")
                        .add("endTime")
                        .add("units", u -> u
                                .add("post")
                                .add("rank")
                                .add("user", user -> user
                                        .add("lastName")
                                        .add("firstName")
                                        .add("patronymic")))
                        .add("administrativeViolations", av -> av.add("article"))
                        .add("criminalViolations", cv -> cv.add("type")));

        List<Shift> all = loader.list();
        if (department == null) {
            return all;
        }
        return all.stream()
                .filter(s -> department.equals(s.getDepartmentToday()))
                .toList();
    }

    private static int countAdministrative(Shift shift) {
        Set<AdministrativeViolation> violations = shift.getAdministrativeViolations();
        return violations == null ? 0 : violations.size();
    }

    private static int countCriminal(Shift shift) {
        Set<CriminalViolation> violations = shift.getCriminalViolations();
        return violations == null ? 0 : violations.size();
    }

    private static int countAdminArticles(Shift shift, Set<ArticleOfAdministrative> articles) {
        Set<AdministrativeViolation> violations = shift.getAdministrativeViolations();
        if (violations == null || violations.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (AdministrativeViolation violation : violations) {
            ArticleOfAdministrative article = violation.getArticle();
            if (article != null && articles.contains(article)) {
                count++;
            }
        }
        return count;
    }

    private static int countCriminalType(Shift shift, TypeOfCriminal type) {
        Set<CriminalViolation> violations = shift.getCriminalViolations();
        if (violations == null || violations.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (CriminalViolation violation : violations) {
            if (type.equals(violation.getType())) {
                count++;
            }
        }
        return count;
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
                .sorted(EmployeeOrdering.serviceInfoComparator())
                .map(ServiceInfo::getUser)
                .filter(Objects::nonNull)
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

    private enum PeriodCountKind {
        CRIMINAL_TYPE,
        ADMIN_ARTICLE,
        ADMIN_ARTICLES,
        STATEMENTS,
        IBDR,
        CLAIMS,
        IMPACT,
        MINORS
    }

    private enum ShiftScalarField {
        STATEMENTS,
        IBDR,
        CLAIMS
    }
}
