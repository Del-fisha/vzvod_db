package com.company.vzvod.service;

import com.company.vzvod.bot.dto.BotLabeledCount;
import com.company.vzvod.bot.dto.BotTodayDashboardResponse;
import com.company.vzvod.bot.dto.BotTodayRouteRow;
import com.company.vzvod.bot.dto.BotTodayTotals;
import com.company.vzvod.entity.AdministrativeViolation;
import com.company.vzvod.entity.ArticleOfAdministrative;
import com.company.vzvod.entity.CriminalViolation;
import com.company.vzvod.entity.Dep;
import com.company.vzvod.entity.NumberOfShift;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.entity.TypeOfCriminal;
import com.company.vzvod.entity.TypeOfShift;
import com.company.vzvod.entity.User;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlanBuilder;
import io.jmix.core.FluentLoader;
import io.jmix.core.UnconstrainedDataManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.mockito.ArgumentMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TodayShiftDashboardService.loadBotDashboard")
class TodayShiftDashboardServiceBotDashboardTest {

    @Mock
    private DataManager dataManager;

    @Mock
    private UnconstrainedDataManager unconstrainedDataManager;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private TodayShiftDashboardService service;

    @BeforeEach
    void stubMessages() {
        lenient().when(messageSource.getMessage(anyString(), isNull(), any(Locale.class)))
                .thenAnswer(invocation -> {
                    String code = invocation.getArgument(0);
                    int dot = code.lastIndexOf('.');
                    return dot >= 0 ? code.substring(dot + 1) : code;
                });
    }

    @Test
    @DisplayName("formatAdministrativeArticleLabel: _20_20 -> 20.20, ANOTHER -> иные")
    void formatAdministrativeArticleLabel_formatsArticleCodes() {
        assertEquals("20.20", TodayShiftDashboardService.formatAdministrativeArticleLabel(ArticleOfAdministrative._20_20));
        assertEquals("18.8", TodayShiftDashboardService.formatAdministrativeArticleLabel(ArticleOfAdministrative._18_8));
        assertEquals("20.1.2", TodayShiftDashboardService.formatAdministrativeArticleLabel(ArticleOfAdministrative._20_1_2));
        assertEquals("иные", TodayShiftDashboardService.formatAdministrativeArticleLabel(ArticleOfAdministrative.ANOTHER));
        assertNull(TodayShiftDashboardService.formatAdministrativeArticleLabel(null));
    }

    @Test
    @DisplayName("группирует статьи КоАП и типы преступлений по маршрутам и в общий итог")
    void loadBotDashboard_aggregatesLabeledCountsPerRouteAndTotal() {
        LocalDate date = LocalDate.of(2026, 3, 20);

        Shift shift = shift(NumberOfShift._28, date, 3, 2, 5, 1, employee("Иванов", "Иван", "Иванович"));
        addAdministrativeViolation(shift, ArticleOfAdministrative._20_20);
        addAdministrativeViolation(shift, ArticleOfAdministrative._20_20);
        addAdministrativeViolation(shift, ArticleOfAdministrative.ANOTHER);
        addCriminalViolation(shift, TypeOfCriminal.HOT_PURSUIT);

        stubShiftLoad(date, List.of(shift));

        BotTodayDashboardResponse response = service.loadBotDashboard(date, Dep.FIRST);

        assertEquals(date, response.operationalDate());
        assertEquals(1, response.departmentNumber());
        assertEquals(1, response.routes().size());

        BotTodayRouteRow route = response.routes().get(0);
        assertEquals(NumberOfShift._28.getId(), route.routeLabel());
        assertEquals(3, route.ibdr());
        assertEquals(2, route.migrant());
        assertEquals(5, route.statements());
        assertEquals(1, route.claims());
        assertEquals(3, route.administrative());
        assertEquals(1, route.criminal());
        assertEquals(List.of("Иванов И. И."), route.employees());

        assertTrue(route.administrativeArticles().contains(new BotLabeledCount("20.20", 2)));
        assertTrue(route.administrativeArticles().contains(new BotLabeledCount("иные", 1)));
        assertTrue(route.criminalTypes().contains(new BotLabeledCount("HOT_PURSUIT", 1)));

        BotTodayTotals totals = response.totals();
        assertEquals(3, totals.ibdr());
        assertEquals(2, totals.migrant());
        assertEquals(5, totals.statements());
        assertEquals(1, totals.claims());
        assertEquals(3, totals.administrative());
        assertEquals(1, totals.criminal());
        assertTrue(totals.administrativeArticles().contains(new BotLabeledCount("20.20", 2)));
        assertTrue(totals.administrativeArticles().contains(new BotLabeledCount("иные", 1)));
    }

    @SuppressWarnings("unchecked")
    private void stubShiftLoad(LocalDate date, List<Shift> shifts) {
        FluentLoader<Shift> loader = mock(FluentLoader.class);
        FluentLoader.ByQuery<Shift> query = mock(FluentLoader.ByQuery.class);
        when(unconstrainedDataManager.load(Shift.class)).thenReturn(loader);
        when(loader.query(any())).thenReturn(query);
        when(query.parameter(eq("date"), eq(date))).thenReturn(query);
        when(query.fetchPlan(ArgumentMatchers.<Consumer<FetchPlanBuilder>>any())).thenReturn(query);
        when(query.list()).thenReturn(shifts);
    }

    private static Shift shift(
            NumberOfShift number,
            LocalDate date,
            int ibdr,
            int migrant,
            int statements,
            int claims,
            ServiceInfo... units
    ) {
        Shift shift = new Shift();
        shift.setId(UUID.randomUUID());
        shift.setNumber(number);
        shift.setDate(date);
        shift.setDepartmentToday(Dep.FIRST);
        shift.setTypeOfShift(TypeOfShift.VZVOD_ROUTE);
        shift.setStartTime(LocalTime.of(9, 0));
        shift.setIbdr(ibdr);
        shift.setMigrant(migrant);
        shift.setCountOfStatements(statements);
        shift.setCountOfClaims(claims);
        shift.setAdministrativeViolations(new HashSet<>());
        shift.setCriminalViolations(new HashSet<>());
        Set<ServiceInfo> unitSet = new HashSet<>(List.of(units));
        shift.setUnits(unitSet);
        return shift;
    }

    private static ServiceInfo employee(String lastName, String firstName, String patronymic) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setLastName(lastName);
        user.setFirstName(firstName);
        user.setPatronymic(patronymic);
        ServiceInfo si = new ServiceInfo();
        si.setId(UUID.randomUUID());
        si.setUser(user);
        return si;
    }

    private static void addAdministrativeViolation(Shift shift, ArticleOfAdministrative article) {
        AdministrativeViolation violation = new AdministrativeViolation();
        violation.setId(UUID.randomUUID());
        violation.setArticle(article);
        shift.getAdministrativeViolations().add(violation);
    }

    private static void addCriminalViolation(Shift shift, TypeOfCriminal type) {
        CriminalViolation violation = new CriminalViolation();
        violation.setId(UUID.randomUUID());
        violation.setType(type);
        shift.getCriminalViolations().add(violation);
    }
}
