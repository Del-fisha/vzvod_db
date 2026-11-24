package com.company.vzvod.util;

import com.company.vzvod.entity.AdministrativeViolation;
import com.company.vzvod.entity.ArticleOfAdministrative;
import com.company.vzvod.entity.CriminalViolation;
import com.company.vzvod.entity.Incentive;
import com.company.vzvod.entity.Penalty;
import com.company.vzvod.entity.Post;
import com.company.vzvod.entity.Rank;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.entity.TypeOfCriminal;
import com.company.vzvod.entity.User;
import com.company.vzvod.entity.Vocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectoryListOrderTest {

    @Test
    @DisplayName("Сотрудники: должность, затем звание по убыванию, затем фамилия")
    void employeesByPostRankAndLastName() {
        User captainPoliceman = user("Сидоров", Post.POLICEMAN, Rank.CAPTAIN);
        User majorPoliceman = user("Иванов", Post.POLICEMAN, Rank.MAJOR);
        User sergeantPoliceman = user("Петров", Post.POLICEMAN, Rank.SERGEANT);
        User commander = user("Яковлев", Post.COM_VZVOD, Rank.LIEUTENANT);

        List<User> users = new ArrayList<>(List.of(captainPoliceman, sergeantPoliceman, majorPoliceman, commander));
        users.sort(EmployeeOrdering.userComparator());

        assertEquals(List.of(commander, majorPoliceman, captainPoliceman, sergeantPoliceman), users);
    }

    @Test
    @DisplayName("Руководство и отделение: должность, затем фамилия, без звания")
    void rosterByPostThenLastName() {
        User juniorCommander = user("Борисов", Post.COM_OTD, Rank.JUNIOR_LIEUTENANT);
        User seniorCommander = user("Андреев", Post.COM_OTD, Rank.MAJOR);
        User policeman = user("Алексеев", Post.POLICEMAN, Rank.COLONEL);

        List<User> users = new ArrayList<>(List.of(policeman, juniorCommander, seniorCommander));
        users.sort(EmployeeOrdering.userByPostThenLastName());

        assertEquals(List.of(seniorCommander, juniorCommander, policeman), users);
    }

    @Test
    @DisplayName("Отпуска: будущее начало сверху, затем конец, должность и фамилия")
    void vocationsFutureStartFirst() {
        Vocation earlyShort = vocation(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), Post.POLICEMAN, "Яковлев");
        Vocation laterCommander = vocation(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 20), Post.COM_OTD, "Сидоров");
        Vocation laterPoliceman = vocation(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 20), Post.POLICEMAN, "Андреев");
        Vocation laterSamePostEarlierEnd = vocation(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 10), Post.COM_OTD, "Борисов");

        List<Vocation> rows = new ArrayList<>(List.of(
                earlyShort, laterPoliceman, laterSamePostEarlierEnd, laterCommander));
        rows.sort(DirectoryListOrder.vocationOrder());

        assertEquals(List.of(laterCommander, laterPoliceman, laterSamePostEarlierEnd, earlyShort), rows);
    }

    @Test
    @DisplayName("Административка: будущая дата сверху, затем номер статьи")
    void administrativeByDateThenArticle() {
        AdministrativeViolation old = admin(LocalDate.of(2020, 1, 1), ArticleOfAdministrative._11_15);
        AdministrativeViolation futureLateArticle = admin(LocalDate.of(2026, 5, 1), ArticleOfAdministrative._20_25);
        AdministrativeViolation futureEarlyArticle = admin(LocalDate.of(2026, 5, 1), ArticleOfAdministrative._18_8);

        List<AdministrativeViolation> rows = new ArrayList<>(List.of(old, futureLateArticle, futureEarlyArticle));
        rows.sort(DirectoryListOrder.administrativeOrder());

        assertEquals(List.of(futureEarlyArticle, futureLateArticle, old), rows);
    }

    @Test
    @DisplayName("Уголовка: будущая дата сверху, затем подпись типа")
    void criminalByDateThenArticle() {
        CriminalViolation old = criminal(LocalDate.of(2021, 3, 1), TypeOfCriminal.HOT_PURSUIT);
        CriminalViolation futureWanted = criminal(LocalDate.of(2026, 4, 1), TypeOfCriminal.FEDERAL_WANTED);
        CriminalViolation futureLocal = criminal(LocalDate.of(2026, 4, 1), TypeOfCriminal.LOCAL_SEARCH);

        List<CriminalViolation> rows = new ArrayList<>(List.of(old, futureWanted, futureLocal));
        rows.sort(DirectoryListOrder.criminalOrder());

        assertEquals(List.of(futureLocal, futureWanted, old), rows);
    }

    @Test
    @DisplayName("Взыскания и поощрения: будущая дата сверху, затем фамилия")
    void rewardsAndPenaltiesByDateThenLastName() {
        Penalty older = penalty(LocalDate.of(2022, 1, 1), "Андреев");
        Penalty futureSecond = penalty(LocalDate.of(2026, 2, 2), "Сидоров");
        Penalty futureFirst = penalty(LocalDate.of(2026, 2, 2), "Борисов");
        List<Penalty> penalties = new ArrayList<>(List.of(older, futureSecond, futureFirst));
        penalties.sort(DirectoryListOrder.penaltyByDateThenLastName());
        assertEquals(List.of(futureFirst, futureSecond, older), penalties);

        Incentive olderIncentive = incentive(LocalDate.of(2023, 1, 1), "Яковлев");
        Incentive future = incentive(LocalDate.of(2026, 8, 1), "Андреев");
        List<Incentive> incentives = new ArrayList<>(List.of(olderIncentive, future));
        incentives.sort(DirectoryListOrder.incentiveByDateThenLastName());
        assertEquals(List.of(future, olderIncentive), incentives);
    }

    @Test
    @DisplayName("Мои смены: будущая дата сверху")
    void shiftsFutureFirst() {
        Shift past = shift(LocalDate.of(2020, 1, 1));
        Shift future = shift(LocalDate.of(2026, 12, 1));
        Shift middle = shift(LocalDate.of(2024, 5, 5));
        List<Shift> shifts = new ArrayList<>(List.of(past, future, middle));
        shifts.sort(DirectoryListOrder.shiftByDate());
        assertEquals(List.of(future, middle, past), shifts);
    }

    @Test
    void pageKeepsRequestedWindow() {
        List<String> all = List.of("a", "b", "c", "d");
        assertEquals(List.of("b", "c"), SortedListLoader.page(all, 1, 2));
        assertEquals(List.of("c", "d"), SortedListLoader.page(all, 2, 0));
        assertEquals(List.of(), SortedListLoader.page(all, 9, 2));
    }

    private static User user(String lastName, Post post, Rank rank) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setLastName(lastName);
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setPost(post);
        serviceInfo.setRank(rank);
        serviceInfo.setUser(user);
        user.setServiceInfo(serviceInfo);
        return user;
    }

    private static Vocation vocation(LocalDate start, LocalDate end, Post post, String lastName) {
        Vocation vocation = new Vocation();
        vocation.setId(UUID.randomUUID());
        vocation.setStartDate(start);
        vocation.setEndDate(end);
        vocation.setUserServiceInfo(serviceInfo(post, lastName));
        return vocation;
    }

    private static ServiceInfo serviceInfo(Post post, String lastName) {
        User user = new User();
        user.setLastName(lastName);
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setPost(post);
        serviceInfo.setUser(user);
        return serviceInfo;
    }

    private static AdministrativeViolation admin(LocalDate date, ArticleOfAdministrative article) {
        AdministrativeViolation violation = new AdministrativeViolation();
        violation.setId(UUID.randomUUID());
        violation.setArticle(article);
        violation.setShift(shift(date));
        return violation;
    }

    private static CriminalViolation criminal(LocalDate date, TypeOfCriminal type) {
        CriminalViolation violation = new CriminalViolation();
        violation.setId(UUID.randomUUID());
        violation.setType(type);
        violation.setShift(shift(date));
        return violation;
    }

    private static Penalty penalty(LocalDate date, String lastName) {
        Penalty penalty = new Penalty();
        penalty.setId(UUID.randomUUID());
        penalty.setDate(date);
        penalty.setUserServiceInfo(serviceInfo(Post.POLICEMAN, lastName));
        return penalty;
    }

    private static Incentive incentive(LocalDate date, String lastName) {
        Incentive incentive = new Incentive();
        incentive.setId(UUID.randomUUID());
        incentive.setDate(date);
        incentive.setUserServiceInfo(serviceInfo(Post.POLICEMAN, lastName));
        return incentive;
    }

    private static Shift shift(LocalDate date) {
        Shift shift = new Shift();
        shift.setId(UUID.randomUUID());
        shift.setDate(date);
        return shift;
    }
}
