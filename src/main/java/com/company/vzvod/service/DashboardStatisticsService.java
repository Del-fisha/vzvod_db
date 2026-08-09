package com.company.vzvod.service;

import com.company.vzvod.dashboard.stats.*;
import com.company.vzvod.entity.AdministrativeViolation;
import com.company.vzvod.entity.ArticleOfAdministrative;
import com.company.vzvod.entity.Department;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.TypeOfCriminal;
import com.company.vzvod.entity.User;
import io.jmix.core.DataManager;
import io.jmix.core.FluentValueLoader;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class DashboardStatisticsService {

    private final DataManager dataManager;

    public DashboardStatisticsService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public StatsResult loadStats(StatsQuery query) {
        query.validate();
        LocalDate ref = query.resolveReferenceDate();
        OverallRange overall = resolveOverallRange(query, ref);
        List<Bucket> buckets = buildBuckets(query, overall, ref);
        List<StatsSeries> series = switch (query.compareMode()) {
            case DEPARTMENTS -> buildDepartmentSeries(query, buckets);
            case EMPLOYEES -> buildEmployeeSeries(query, buckets);
        };
        EmployeePeriodTotals totals = maybeEmployeeTotals(query, overall);
        List<String> labels = buckets.stream().map(Bucket::label).toList();
        return new StatsResult(labels, series, totals);
    }

    @Nullable
    private EmployeePeriodTotals maybeEmployeeTotals(StatsQuery query, OverallRange overall) {
        if (query.period() == StatsPeriod.TODAY) {
            return null;
        }
        if (query.compareMode() != StatsCompareMode.EMPLOYEES || query.employeeUserIds().size() != 1) {
            return null;
        }
        UUID userId = query.employeeUserIds().iterator().next();
        ServiceInfo si = loadServiceInfoForUser(userId);
        if (si == null) {
            return null;
        }
        long adm = query.metrics().contains(WorkMetric.ADMINISTRATIVE_VIOLATIONS)
                ? countAdministrative(si, overall.from(), overall.to(), query) : 0;
        long cr = query.metrics().contains(WorkMetric.CRIMINAL_VIOLATIONS)
                ? countCriminal(si, overall.from(), overall.to(), query) : 0;
        long ibd = query.metrics().contains(WorkMetric.IBDR)
                ? sumIbdr(si, overall.from(), overall.to()) : 0;
        return new EmployeePeriodTotals(userId, adm, cr, ibd);
    }

    private List<StatsSeries> buildDepartmentSeries(StatsQuery query, List<Bucket> buckets) {
        List<UUID> deptOrder = query.departmentIds().stream().sorted().toList();
        List<StatsSeries> out = new ArrayList<>();
        for (UUID deptId : deptOrder) {
            Department d = dataManager.load(Department.class).id(deptId).optional().orElse(null);
            String label = d != null ? ("№ " + Objects.toString(d.getNumber(), "?")) : deptId.toString();
            double[] vals = new double[buckets.size()];
            for (int i = 0; i < buckets.size(); i++) {
                Bucket b = buckets.get(i);
                vals[i] = sumMetricsForDepartment(deptId, b.from(), b.to(), query);
            }
            out.add(new StatsSeries(deptId, label, vals));
        }
        return out;
    }

    private List<StatsSeries> buildEmployeeSeries(StatsQuery query, List<Bucket> buckets) {
        List<UUID> userOrder = query.employeeUserIds().stream().sorted().toList();
        List<StatsSeries> out = new ArrayList<>();
        for (UUID userId : userOrder) {
            User u = dataManager.load(User.class).id(userId).optional().orElse(null);
            String label = u != null ? u.getShortFio() : userId.toString();
            ServiceInfo si = loadServiceInfoForUser(userId);
            double[] vals = new double[buckets.size()];
            for (int i = 0; i < buckets.size(); i++) {
                Bucket b = buckets.get(i);
                vals[i] = si == null ? 0.0 : sumMetricsForServiceInfo(si, b.from(), b.to(), query);
            }
            out.add(new StatsSeries(userId, label, vals));
        }
        return out;
    }

    private double sumMetricsForDepartment(UUID departmentId, LocalDate from, LocalDate to, StatsQuery query) {
        // В режиме "общие показатели отделения" считаем по нарядам (Shift), а не по каждому сотруднику.
        // Иначе один и тот же наряд (где несколько сотрудников одного отделения) будет ошибочно давать +N к отделению.
        double sum = 0.0;
        if (query.metrics().contains(WorkMetric.ADMINISTRATIVE_VIOLATIONS)) {
            sum += countAdministrativeDistinctViolationsForDepartment(departmentId, from, to, query);
        }
        if (query.metrics().contains(WorkMetric.CRIMINAL_VIOLATIONS)) {
            sum += countCriminalDistinctViolationsForDepartment(departmentId, from, to, query);
        }
        if (query.metrics().contains(WorkMetric.IBDR)) {
            sum += sumIbdrDistinctShiftsForDepartment(departmentId, from, to);
        }
        return sum;
    }

    private double sumMetricsForServiceInfo(ServiceInfo si, LocalDate from, LocalDate to, StatsQuery query) {
        double sum = 0.0;
        if (query.metrics().contains(WorkMetric.ADMINISTRATIVE_VIOLATIONS)) {
            sum += countAdministrative(si, from, to, query);
        }
        if (query.metrics().contains(WorkMetric.CRIMINAL_VIOLATIONS)) {
            sum += countCriminal(si, from, to, query);
        }
        if (query.metrics().contains(WorkMetric.IBDR)) {
            sum += sumIbdr(si, from, to);
        }
        return sum;
    }

    private ServiceInfo loadServiceInfoForUser(UUID userId) {
        return dataManager.load(ServiceInfo.class)
                .query("select e from ServiceInfo e where e.user.id = :uid")
                .parameter("uid", userId)
                .optional()
                .orElse(null);
    }

    private long countAdministrative(ServiceInfo si, LocalDate from, LocalDate to, StatsQuery query) {
        List<ArticleOfAdministrative> articlesFilter = mapArticles(query.administrativeArticleIds());
        if (!query.administrativeArticleIds().isEmpty() && articlesFilter.isEmpty()) {
            return 0;
        }
        String jpql = """
                select v from AdministrativeViolation v
                join v.shift s
                join s.units u
                where u.id = :svcInfoPk
                  and s.date >= :from and s.date <= :to
                """;
        List<AdministrativeViolation> list = dataManager.load(AdministrativeViolation.class)
                .query(jpql)
                .parameter("svcInfoPk", si.getId())
                .parameter("from", from)
                .parameter("to", to)
                .list();
        if (articlesFilter.isEmpty()) {
            return list.size();
        }
        return list.stream()
                .filter(v -> v.getArticle() != null && articlesFilter.contains(v.getArticle()))
                .count();
    }

    private long countAdministrativeDistinctViolationsForDepartment(UUID departmentId, LocalDate from, LocalDate to, StatsQuery query) {
        List<ArticleOfAdministrative> articlesFilter = mapArticles(query.administrativeArticleIds());
        if (!query.administrativeArticleIds().isEmpty() && articlesFilter.isEmpty()) {
            return 0;
        }
        String jpql = """
                select distinct v.id as vid
                from AdministrativeViolation v
                join v.shift s
                join s.units u
                where u.department.id = :dep
                  and s.date >= :from and s.date <= :to
                """ + (articlesFilter.isEmpty() ? "" : " and v.article in :arts ");
        var loader = dataManager.loadValues(jpql)
                .properties("vid")
                .parameter("dep", departmentId)
                .parameter("from", from)
                .parameter("to", to);
        if (!articlesFilter.isEmpty()) {
            loader.parameter("arts", articlesFilter);
        }
        return loader.list().size();
    }

    private long countCriminal(ServiceInfo si, LocalDate from, LocalDate to, StatsQuery query) {
        String jpql = """
                select count(v)
                from CriminalViolation v join v.shift s join s.units u
                where u.id = :svcInfoPk
                  and s.date >= :from and s.date <= :to
                """ + (query.criminalTypeIds().isEmpty() ? "" : " and v.type in :types ");
        var loader = dataManager.loadValue(jpql, Long.class)
                .parameter("svcInfoPk", si.getId())
                .parameter("from", from)
                .parameter("to", to);
        if (!query.criminalTypeIds().isEmpty()) {
            loader.parameter("types", query.criminalTypeIds().stream().toList());
        }
        Long v = loader.one();
        return v == null ? 0L : v;
    }

    private long countCriminalDistinctViolationsForDepartment(UUID departmentId, LocalDate from, LocalDate to, StatsQuery query) {
        String jpql = """
                select distinct v.id as vid
                from CriminalViolation v
                join v.shift s
                join s.units u
                where u.department.id = :dep
                  and s.date >= :from and s.date <= :to
                """ + (query.criminalTypeIds().isEmpty() ? "" : " and v.type in :types ");
        var loader = dataManager.loadValues(jpql)
                .properties("vid")
                .parameter("dep", departmentId)
                .parameter("from", from)
                .parameter("to", to);
        if (!query.criminalTypeIds().isEmpty()) {
            loader.parameter("types", query.criminalTypeIds().stream().toList());
        }
        return loader.list().size();
    }

    /**
     * Фильтр статей: пустой набор = все; иначе только переданные (известные) id.
     */
    private List<ArticleOfAdministrative> mapArticles(Set<Integer> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        List<ArticleOfAdministrative> out = new ArrayList<>();
        for (Integer id : ids) {
            ArticleOfAdministrative a = ArticleOfAdministrative.fromId(id);
            if (a != null) {
                out.add(a);
            }
        }
        return out;
    }

    private long sumIbdr(ServiceInfo si, LocalDate from, LocalDate to) {
        String jpql = """
                select coalesce(sum(coalesce(s.ibdr, 0)), 0)
                from Shift s join s.units u
                where u.id = :svcInfoPk
                  and s.date >= :from and s.date <= :to
                """;
        Long v = dataManager.loadValue(jpql, Long.class)
                .parameter("svcInfoPk", si.getId())
                .parameter("from", from)
                .parameter("to", to)
                .one();
        return v == null ? 0L : v;
    }

    private long sumIbdrDistinctShiftsForDepartment(UUID departmentId, LocalDate from, LocalDate to) {
        // В Shift.ibdr хранится итог по наряду — суммируем один раз на Shift.
        String jpql = """
                select distinct s.id as sid, coalesce(s.ibdr, 0) as v
                from Shift s
                join s.units u
                where u.department.id = :dep
                  and s.date >= :from and s.date <= :to
                """;
        return dataManager.loadValues(jpql)
                .properties("sid", "v")
                .parameter("dep", departmentId)
                .parameter("from", from)
                .parameter("to", to)
                .list()
                .stream()
                .mapToLong(r -> {
                    Number v = (Number) r.getValue("v");
                    return v == null ? 0L : v.longValue();
                })
                .sum();
    }

    private List<Bucket> buildBuckets(StatsQuery query, OverallRange overall, LocalDate ref) {
        return switch (query.period()) {
            case TODAY -> List.of(new Bucket(overall.from(), overall.to(), ref.format(DateTimeFormatter.ISO_LOCAL_DATE)));
            case MONTH -> buildMonthDayBuckets(overall);
            case YEAR -> buildYearMonthBuckets(overall, ref);
            case ALL_TIME -> buildAllTimeYearBuckets(query, overall, ref);
        };
    }

    private List<Bucket> buildMonthDayBuckets(OverallRange overall) {
        List<Bucket> list = new ArrayList<>();
        for (LocalDate d = overall.from(); !d.isAfter(overall.to()); d = d.plusDays(1)) {
            list.add(new Bucket(d, d, String.valueOf(d.getDayOfMonth())));
        }
        return list;
    }

    private List<Bucket> buildYearMonthBuckets(OverallRange overall, LocalDate ref) {
        List<Bucket> list = new ArrayList<>();
        int y = ref.getYear();
        for (Month m : Month.values()) {
            YearMonth ym = YearMonth.of(y, m);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();
            if (end.isBefore(overall.from()) || start.isAfter(overall.to())) {
                continue;
            }
            LocalDate bFrom = start.isBefore(overall.from()) ? overall.from() : start;
            LocalDate bTo = end.isAfter(overall.to()) ? overall.to() : end;
            String label = ym.getMonth().getDisplayName(java.time.format.TextStyle.SHORT, new Locale("ru"));
            list.add(new Bucket(bFrom, bTo, label));
        }
        return list;
    }

    private List<Bucket> buildAllTimeYearBuckets(StatsQuery query, OverallRange overall, LocalDate ref) {
        LocalDate min = findMinShiftDate(query);
        LocalDate max = findMaxShiftDate(query);
        if (min == null || max == null) {
            return List.of();
        }
        int y1 = min.getYear();
        int y2 = max.getYear();
        List<Bucket> list = new ArrayList<>();
        for (int y = y1; y <= y2; y++) {
            LocalDate start = LocalDate.of(y, 1, 1);
            LocalDate end = LocalDate.of(y, 12, 31);
            if (end.isBefore(overall.from()) || start.isAfter(overall.to())) {
                continue;
            }
            LocalDate bFrom = start.isBefore(overall.from()) ? overall.from() : start;
            LocalDate bTo = end.isAfter(overall.to()) ? overall.to() : end;
            list.add(new Bucket(bFrom, bTo, String.valueOf(y)));
        }
        return list.isEmpty()
                ? List.of(new Bucket(overall.from(), overall.to(), String.valueOf(ref.getYear())))
                : list;
    }

    private LocalDate findMinShiftDate(StatsQuery query) {
        String jpql = """
                select min(s.date)
                from Shift s
                join s.units u
                where %s
                """.formatted(scopeWhereClause(query));
        var loader = dataManager.loadValue(jpql, LocalDate.class);
        bindScopeParams(query, loader);
        return loader.optional().orElse(null);
    }

    private LocalDate findMaxShiftDate(StatsQuery query) {
        String jpql = """
                select max(s.date)
                from Shift s
                join s.units u
                where %s
                """.formatted(scopeWhereClause(query));
        var loader = dataManager.loadValue(jpql, LocalDate.class);
        bindScopeParams(query, loader);
        return loader.optional().orElse(null);
    }

    private String scopeWhereClause(StatsQuery query) {
        return switch (query.compareMode()) {
            case DEPARTMENTS -> "u.department.id in :deptIds";
            case EMPLOYEES -> "u.user.id in :userIds";
        };
    }

    private void bindScopeParams(StatsQuery query, FluentValueLoader<?> loader) {
        switch (query.compareMode()) {
            case DEPARTMENTS -> loader.parameter("deptIds", query.departmentIds().stream().toList());
            case EMPLOYEES -> loader.parameter("userIds", query.employeeUserIds().stream().toList());
        }
    }

    private OverallRange resolveOverallRange(StatsQuery query, LocalDate ref) {
        StatsPeriod period = query.period();
        return switch (period) {
            case TODAY -> new OverallRange(ref, ref);
            case MONTH -> {
                LocalDate start = ref.withDayOfMonth(1);
                LocalDate end = ref.withDayOfMonth(ref.lengthOfMonth());
                yield new OverallRange(start, end);
            }
            case YEAR -> {
                LocalDate start = ref.withDayOfYear(1);
                LocalDate end = ref.withMonth(12).withDayOfMonth(31);
                yield new OverallRange(start, end);
            }
            case ALL_TIME -> {
                // Для "всё время" верхняя граница должна включать все смены в выбранном скоупе,
                // а не обрезаться текущей датой (иначе "за месяц" может быть больше "за всё время"). 
                LocalDate max = findMaxShiftDate(query);
                LocalDate end = max != null ? max : ref;
                yield new OverallRange(LocalDate.of(1970, 1, 1), end);
            }
        };
    }

    /** Поддержка UI: все статьи и типы из enum для чекбоксов «по умолчанию все». */
    public AdministrativeArticleOptionDto[] allAdministrativeArticleOptions() {
        return java.util.Arrays.stream(ArticleOfAdministrative.values())
                .sorted(Comparator.comparing(ArticleOfAdministrative::getId))
                .map(a -> new AdministrativeArticleOptionDto(a.getId(), a.name()))
                .toArray(AdministrativeArticleOptionDto[]::new);
    }

    public CriminalTypeOptionDto[] allCriminalTypeOptions() {
        return java.util.Arrays.stream(TypeOfCriminal.values())
                .sorted(Comparator.comparing(TypeOfCriminal::getId))
                .map(t -> new CriminalTypeOptionDto(t.getId(), t.name()))
                .toArray(CriminalTypeOptionDto[]::new);
    }

    /** DTO для привязки к UI без тянуть enum в представление напрямую. */
    public record AdministrativeArticleOptionDto(int id, String enumName) {
    }

    public record CriminalTypeOptionDto(int id, String enumName) {
    }

    private record OverallRange(LocalDate from, LocalDate to) {
    }

    private record Bucket(LocalDate from, LocalDate to, String label) {
    }

    /**
     * Слева дерево выбора: отделение и его сотрудники (Фамилия И.О.) для диалога.
     */
    public List<DepartmentEmployeesRow> loadDepartmentEmployeeTree() {
        List<Department> departments = dataManager.load(Department.class)
                .query("select d from Department d order by d.number")
                .list();
        List<DepartmentEmployeesRow> rows = new ArrayList<>();
        for (Department d : departments) {
            List<ServiceInfo> sis = dataManager.load(ServiceInfo.class)
                    .query("select e from ServiceInfo e where e.department.id = :id order by e.post, e.rank desc, e.user.lastName")
                    .parameter("id", d.getId())
                    .list();
            LinkedHashMap<UUID, String> employeeLabels = new LinkedHashMap<>();
            for (ServiceInfo si : sis) {
                User user = si.getUser();
                if (user != null && user.getId() != null) {
                    employeeLabels.put(user.getId(), user.getShortFio());
                }
            }
            rows.add(new DepartmentEmployeesRow(d.getId(), d.getNumber(), employeeLabels));
        }
        rows.sort(Comparator.comparing(r -> Objects.requireNonNullElse(r.departmentNumber(), Integer.MAX_VALUE)));
        return rows;
    }

    public record DepartmentEmployeesRow(UUID departmentId, Integer departmentNumber, LinkedHashMap<UUID, String> employeesByUserId) {
    }
}
