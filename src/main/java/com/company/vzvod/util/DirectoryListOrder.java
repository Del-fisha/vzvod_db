package com.company.vzvod.util;

import com.company.vzvod.entity.AdministrativeViolation;
import com.company.vzvod.entity.ArticleOfAdministrative;
import com.company.vzvod.entity.CriminalViolation;
import com.company.vzvod.entity.Incentive;
import com.company.vzvod.entity.Penalty;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.entity.TypeOfCriminal;
import com.company.vzvod.entity.User;
import com.company.vzvod.entity.Vocation;

import java.text.Collator;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Locale;

/**
 * Порядок списков базы и «Мои данные».
 * Даты: более поздние сверху, пустые снизу.
 * Фамилия сравнивается уже расшифрованной, поэтому такие списки сортируются в памяти.
 */
public final class DirectoryListOrder {

    private static final Collator RUSSIAN = Collator.getInstance(Locale.forLanguageTag("ru"));

    static {
        RUSSIAN.setStrength(Collator.PRIMARY);
    }

    private static final Comparator<LocalDate> FUTURE_FIRST =
            Comparator.nullsLast(Comparator.reverseOrder());

    private static final Comparator<String> TEXT =
            Comparator.nullsLast(RUSSIAN);

    private DirectoryListOrder() {
    }

    public static Comparator<Shift> shiftByDate() {
        return Comparator
                .comparing(Shift::getDate, FUTURE_FIRST)
                .thenComparing(Shift::getId, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    public static Comparator<Vocation> vocationOrder() {
        return Comparator
                .comparing(Vocation::getStartDate, FUTURE_FIRST)
                .thenComparing(Vocation::getEndDate, FUTURE_FIRST)
                .thenComparing(DirectoryListOrder::vocationPost, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(DirectoryListOrder::vocationLastName, TEXT)
                .thenComparing(Vocation::getId, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    public static Comparator<AdministrativeViolation> administrativeByDate() {
        return Comparator
                .comparing(DirectoryListOrder::violationDate, FUTURE_FIRST)
                .thenComparing(AdministrativeViolation::getId, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    public static Comparator<CriminalViolation> criminalByDate() {
        return Comparator
                .comparing(DirectoryListOrder::criminalDate, FUTURE_FIRST)
                .thenComparing(CriminalViolation::getId, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    public static Comparator<AdministrativeViolation> administrativeOrder() {
        return Comparator
                .comparing(DirectoryListOrder::violationDate, FUTURE_FIRST)
                .thenComparing(violation -> articleKey(violation.getArticle()), TEXT)
                .thenComparing(AdministrativeViolation::getId, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    public static Comparator<CriminalViolation> criminalOrder() {
        return Comparator
                .comparing(DirectoryListOrder::criminalDate, FUTURE_FIRST)
                .thenComparing(violation -> criminalArticleKey(violation.getType()), TEXT)
                .thenComparing(CriminalViolation::getId, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    public static Comparator<Penalty> penaltyByDateThenLastName() {
        return Comparator
                .comparing(Penalty::getDate, FUTURE_FIRST)
                .thenComparing(DirectoryListOrder::penaltyLastName, TEXT)
                .thenComparing(Penalty::getId, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    public static Comparator<Incentive> incentiveByDateThenLastName() {
        return Comparator
                .comparing(Incentive::getDate, FUTURE_FIRST)
                .thenComparing(DirectoryListOrder::incentiveLastName, TEXT)
                .thenComparing(Incentive::getId, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    public static Comparator<Penalty> penaltyByDate() {
        return Comparator
                .comparing(Penalty::getDate, FUTURE_FIRST)
                .thenComparing(Penalty::getId, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    public static Comparator<Incentive> incentiveByDate() {
        return Comparator
                .comparing(Incentive::getDate, FUTURE_FIRST)
                .thenComparing(Incentive::getId, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    static String articleKey(ArticleOfAdministrative article) {
        if (article == null) {
            return null;
        }
        return switch (article) {
            case _11_15 -> "11.15";
            case _18_8 -> "18.8";
            case _19_3 -> "19.3";
            case _20_1 -> "20.1";
            case _20_1_2 -> "20.1 ч.2";
            case _20_20 -> "20.20";
            case _20_21 -> "20.21";
            case _20_25 -> "20.25";
            case ANOTHER -> "Другое";
        };
    }

    static String criminalArticleKey(TypeOfCriminal type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case LOCAL_SEARCH -> "Местный розыск";
            case IDENTIFICATION -> "По ориентировке";
            case HOT_PURSUIT -> "Преступление";
            case WATCH_LIST -> "Сторожевой учёт";
            case FEDERAL_WANTED -> "Федеральный розыск";
        };
    }

    private static LocalDate violationDate(AdministrativeViolation violation) {
        return violation == null || violation.getShift() == null ? null : violation.getShift().getDate();
    }

    private static LocalDate criminalDate(CriminalViolation violation) {
        return violation == null || violation.getShift() == null ? null : violation.getShift().getDate();
    }

    private static String vocationPost(Vocation vocation) {
        return EmployeeOrdering.postSortKey(vocation == null ? null : vocation.getUserServiceInfo());
    }

    private static String vocationLastName(Vocation vocation) {
        return lastName(vocation == null ? null : vocation.getUserServiceInfo());
    }

    private static String penaltyLastName(Penalty penalty) {
        return lastName(penalty == null ? null : penalty.getUserServiceInfo());
    }

    private static String incentiveLastName(Incentive incentive) {
        return lastName(incentive == null ? null : incentive.getUserServiceInfo());
    }

    private static String lastName(ServiceInfo serviceInfo) {
        User user = serviceInfo == null ? null : serviceInfo.getUser();
        return EmployeeOrdering.lastNameSortKey(user);
    }
}
