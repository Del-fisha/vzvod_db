package com.company.vzvod.integration;

import com.company.vzvod.dashboard.stats.*;
import com.company.vzvod.entity.AdministrativeViolation;
import com.company.vzvod.entity.CriminalViolation;
import com.company.vzvod.entity.ArticleOfAdministrative;
import com.company.vzvod.entity.Department;
import com.company.vzvod.entity.Impact;
import com.company.vzvod.entity.NumberOfShift;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.entity.TypeOfCriminal;
import com.company.vzvod.entity.TypeOfShift;
import com.company.vzvod.entity.User;
import com.company.vzvod.entity.IdCard;
import com.company.vzvod.service.DepartmentConverter;
import com.company.vzvod.service.DashboardStatisticsService;
import com.company.vzvod.test_support.PreTestEntities;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test-postgres")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("DashboardStatisticsService: интеграция")
class DashboardStatisticsServiceIntegrationTest {

    @Autowired
    DataManager dataManager;

    @Autowired
    SystemAuthenticator systemAuthenticator;

    @Autowired
    DashboardStatisticsService dashboardStatisticsService;

    @Autowired
    PasswordEncoder passwordEncoder;

    private static final LocalDate REF = LocalDate.of(2026, 5, 10);

    @BeforeEach
    void setup() {
        systemAuthenticator.begin();
    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("EMPLOYEES + MONTH: два сотрудника, сумма метрик по дням в пределах месяца")
    void employeesMonth_twoSeries() {
        Department d1 = saveDepartment(101);
        Department d2 = saveDepartment(102);
        User u1 = saveUserWithServiceInfo(d1);
        User u2 = saveUserWithServiceInfo(d2);
        ServiceInfo si1 = u1.getServiceInfo();
        ServiceInfo si2 = u2.getServiceInfo();

        Shift shift1 = shiftOn(REF, si1);
        addAdminViolation(shift1, ArticleOfAdministrative._18_8);
        shift1.setIbdr(0);
        dataManager.save(shift1);

        Shift shift2 = shiftOn(REF.minusDays(1), si2);
        addCriminalViolation(shift2, TypeOfCriminal.FEDERAL_WANTED);
        shift2.setIbdr(0);
        dataManager.save(shift2);

        StatsQuery q = new StatsQuery(
                StatsPeriod.MONTH,
                REF,
                EnumSet.allOf(WorkMetric.class),
                Set.of(),
                Set.of(),
                StatsCompareMode.EMPLOYEES,
                Set.of(),
                Set.of(u1.getId(), u2.getId())
        );
        StatsResult r = dashboardStatisticsService.loadStats(q);
        assertFalse(r.bucketLabels().isEmpty());
        assertEquals(2, r.series().size());
        assertNotNull(r.series().get(0).bucketValues());
        double sum1 = java.util.Arrays.stream(r.series().get(0).bucketValues()).sum();
        double sum2 = java.util.Arrays.stream(r.series().get(1).bucketValues()).sum();
        assertEquals(1.0, sum1, 0.01);
        assertEquals(1.0, sum2, 0.01);
    }

    @Test
    @DisplayName("DEPARTMENTS + TODAY: сумма по отделению = сумма сотрудников отделения")
    void departmentsToday_aggregate() {
        Department d = saveDepartment(210);
        User u1 = saveUserWithServiceInfo(d);
        User u2 = saveUserWithServiceInfo(d);
        ServiceInfo si1 = u1.getServiceInfo();
        ServiceInfo si2 = u2.getServiceInfo();

        Shift s1 = shiftOn(REF, si1);
        addAdminViolation(s1, ArticleOfAdministrative._19_3);
        dataManager.save(s1);

        Shift s2 = shiftOn(REF, si2);
        s2.setIbdr(5);
        dataManager.save(s2);

        StatsQuery q = new StatsQuery(
                StatsPeriod.TODAY,
                REF,
                EnumSet.allOf(WorkMetric.class),
                Set.of(),
                Set.of(),
                StatsCompareMode.DEPARTMENTS,
                Set.of(d.getId()),
                Set.of()
        );
        StatsResult r = dashboardStatisticsService.loadStats(q);
        assertEquals(1, r.series().size());
        assertEquals(1, r.bucketLabels().size());
        assertEquals(6.0, r.series().get(0).bucketValues()[0], 0.01);
    }

    @Test
    @DisplayName("Фильтр статей АП: пустой набор = все; иначе только выбранные статьи")
    void administrativeArticleFilter() {
        Department d = saveDepartment(303);
        User u = saveUserWithServiceInfo(d);
        ServiceInfo si = u.getServiceInfo();
        Shift sh = shiftOn(REF, si);
        addAdminViolation(sh, ArticleOfAdministrative._18_8);
        dataManager.save(sh);

        StatsQuery allArticles = new StatsQuery(
                StatsPeriod.TODAY,
                REF,
                EnumSet.of(WorkMetric.ADMINISTRATIVE_VIOLATIONS),
                Set.of(),
                Set.of(),
                StatsCompareMode.EMPLOYEES,
                Set.of(),
                Set.of(u.getId())
        );
        assertEquals(1.0, dashboardStatisticsService.loadStats(allArticles).series().get(0).bucketValues()[0], 0.01);

        StatsQuery otherArticleOnly = new StatsQuery(
                StatsPeriod.TODAY,
                REF,
                EnumSet.of(WorkMetric.ADMINISTRATIVE_VIOLATIONS),
                Set.of(ArticleOfAdministrative._11_15.getId()),
                Set.of(),
                StatsCompareMode.EMPLOYEES,
                Set.of(),
                Set.of(u.getId())
        );
        assertEquals(0.0, dashboardStatisticsService.loadStats(otherArticleOnly).series().get(0).bucketValues()[0], 0.01);

        StatsQuery matchingArticle = new StatsQuery(
                StatsPeriod.TODAY,
                REF,
                EnumSet.of(WorkMetric.ADMINISTRATIVE_VIOLATIONS),
                Set.of(ArticleOfAdministrative._18_8.getId()),
                Set.of(),
                StatsCompareMode.EMPLOYEES,
                Set.of(),
                Set.of(u.getId())
        );
        assertEquals(1.0, dashboardStatisticsService.loadStats(matchingArticle).series().get(0).bucketValues()[0], 0.01);
    }

    @Test
    @DisplayName("Итоги за период для одного сотрудника (не СЕГОДНЯ)")
    void employeePeriodTotals_singleEmployee() {
        Department d = saveDepartment(404);
        User u = saveUserWithServiceInfo(d);
        ServiceInfo si = u.getServiceInfo();

        Shift sh1 = shiftOn(REF.withDayOfMonth(1), si);
        sh1.setIbdr(4);
        addAdminViolation(sh1, ArticleOfAdministrative._11_15);
        dataManager.save(sh1);

        Shift sh2 = shiftOn(REF, si);
        addCriminalViolation(sh2, TypeOfCriminal.WATCH_LIST);
        dataManager.save(sh2);

        StatsQuery q = new StatsQuery(
                StatsPeriod.MONTH,
                REF,
                EnumSet.allOf(WorkMetric.class),
                Set.of(),
                Set.of(),
                StatsCompareMode.EMPLOYEES,
                Set.of(),
                Set.of(u.getId())
        );
        StatsResult r = dashboardStatisticsService.loadStats(q);
        assertNotNull(r.employeeTotalsOrNull());
        assertEquals(u.getId(), r.employeeTotalsOrNull().employeeUserId());
        assertEquals(1, r.employeeTotalsOrNull().administrativeViolations());
        assertEquals(1, r.employeeTotalsOrNull().criminalViolations());
        assertEquals(4, r.employeeTotalsOrNull().ibdr());
    }

    @Test
    @DisplayName("StatsQuery.validate: нет метрик — ошибка")
    void validate_rejectsEmptyMetrics() {
        StatsQuery q = new StatsQuery(
                StatsPeriod.TODAY,
                REF,
                EnumSet.noneOf(WorkMetric.class),
                Set.of(),
                Set.of(),
                StatsCompareMode.EMPLOYEES,
                Set.of(),
                Set.of(UUID.randomUUID())
        );
        assertThrows(IllegalArgumentException.class, q::validate);
    }

    private Department saveDepartment(int number) {
        Department d = dataManager.create(Department.class);
        d.setNumber(number);
        return dataManager.save(d);
    }

    private User saveUserWithServiceInfo(Department department) {
        User user = dataManager.create(User.class);
        PreTestEntities.updateUser(user);
        user.setUsername("st" + System.nanoTime());
        user.setPassword(passwordEncoder.encode("x"));
        user = dataManager.save(user);

        IdCard idCard = dataManager.create(IdCard.class);
        PreTestEntities.updateIdCard(idCard);
        idCard = dataManager.save(idCard);

        ServiceInfo si = dataManager.create(ServiceInfo.class);
        PreTestEntities.updateServiceInfo(si);
        si.setUser(user);
        si.setDepartment(department);
        si.setIdCard(idCard);
        si = dataManager.save(si);
        user.setServiceInfo(si);
        user = dataManager.save(user);
        return user;
    }

    private Shift shiftOn(LocalDate date, ServiceInfo unit) {
        Shift shift = dataManager.create(Shift.class);
        shift.setDepartmentToday(DepartmentConverter.departmentFromDate(date));
        shift.setStartTime(LocalTime.of(8, 0));
        shift.setEndTime(LocalTime.of(20, 0));
        shift.setTypeOfShift(TypeOfShift.VZVOD_ROUTE);
        shift.setNumber(NumberOfShift._28);
        shift.setDate(date);
        shift.setMigrant(0);
        shift.setIbdr(0);
        shift.setCountOfStatements(0);
        shift.setCountOfClaims(0);
        shift.getUnits().add(unit);
        return dataManager.save(shift);
    }

    private void addAdminViolation(Shift shift, ArticleOfAdministrative article) {
        AdministrativeViolation v = dataManager.create(AdministrativeViolation.class);
        v.setShift(shift);
        v.setArticle(article);
        v.setImpact(Impact.WITHOUT_IMPACT);
        dataManager.save(v);
    }

    private void addCriminalViolation(Shift shift, TypeOfCriminal type) {
        CriminalViolation v = dataManager.create(CriminalViolation.class);
        v.setShift(shift);
        v.setType(type);
        v.setImpact(Impact.WITHOUT_IMPACT);
        dataManager.save(v);
    }
}
