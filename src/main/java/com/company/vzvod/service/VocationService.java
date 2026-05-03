package com.company.vzvod.service;

import com.company.vzvod.entity.ArmyService;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Норматив основного отпуска по стажу службы ({@link ServiceInfo#getStartDate()}) с учётом прибавки
 * года за армейскую службу ({@link User#getArmyService()} {@code == SERVED}).
 * <p>
 * Календарные лимиты: до 10 лет включительно (порог считается через {@link ChronoUnit#YEARS} + армия)
 * — 40 дней; после 15 лет включительно — 50 дней; между ними — 45 дней.
 * Смена номинального лимита с 01.01 каждого года опирается на стаж по состоянию на эту дату.
 * Если порог ({@code 10} или {@code 15}) пересекается в течение года, после даты перехода
 * к доступному добавляется дополнительно {@value #MID_YEAR_TIER_DELTA} дней.
 */
public final class VocationService {

    private static final int DAYS_BELOW_TEN_YEAR_THRESHOLD = 40;
    private static final int DAYS_FROM_TEN_TO_FIFTEEN = 45;
    private static final int DAYS_FROM_FIFTEEN = 50;
    /** Дополнительно к доступному после наступления 10-й или 15-й год выслуги внутри календарного года. */
    static final int MID_YEAR_TIER_DELTA = 5;

    private VocationService() {
    }

    public static int effectiveYears(ServiceInfo serviceInfo, LocalDate onDate) {
        if (serviceInfo == null || serviceInfo.getStartDate() == null || onDate == null) {
            return 0;
        }
        int policeYears = (int) ChronoUnit.YEARS.between(serviceInfo.getStartDate(), onDate);
        int army = armyCreditYears(serviceInfo.getUser());
        return policeYears + army;
    }

    /**
     * Плановые дни основного отпуска («положено» на год) только по номинальной норме на заданную дату,
     * без промежуточной надбавки и без дней выезда.
     */
    public static int nominalDaysAvailable(ServiceInfo serviceInfo, LocalDate date) {
        int years = effectiveYears(serviceInfo, date);
        return tierDaysForSeniorityYears(years);
    }

    /**
     * Сумма +{@value #MID_YEAR_TIER_DELTA} за каждый порог из (10 лет, 15 лет), который:
     * <ul>
     *     <li>ещё не был достигнут на {@code yearStartInclusive};</li>
     *     <li>наступает не позже {@code evaluationDateInclusive}</li>
     *     <li>и попадает в текущий календарный год (начало года — {@code yearStartInclusive}).</li>
     * </ul>
     */
    public static int midYearSeniorityBonuses(ServiceInfo serviceInfo,
                                              LocalDate yearStartInclusive,
                                              LocalDate evaluationDateInclusive) {
        if (serviceInfo == null || serviceInfo.getStartDate() == null
                || yearStartInclusive == null || evaluationDateInclusive == null) {
            return 0;
        }
        LocalDate policeStart = serviceInfo.getStartDate();
        LocalDate until = evaluationDateInclusive.isBefore(yearStartInclusive)
                ? yearStartInclusive.minusDays(1)
                : evaluationDateInclusive;
        int armyYears = armyCreditYears(serviceInfo.getUser());

        int bonus = 0;
        LocalDate jan1 = yearStartInclusive;
        LocalDate tenth = policeStart.plusYears(Math.max(0, 10 - armyYears));
        if (effectiveYears(serviceInfo, jan1) < 10
                && !tenth.isBefore(jan1) && !tenth.isAfter(until)) {
            bonus += MID_YEAR_TIER_DELTA;
        }

        LocalDate fifteenth = policeStart.plusYears(Math.max(0, 15 - armyYears));
        if (effectiveYears(serviceInfo, jan1) < 15
                && !fifteenth.isBefore(jan1) && !fifteenth.isAfter(until)) {
            bonus += MID_YEAR_TIER_DELTA;
        }

        return bonus;
    }

    /**
     * @deprecated Используйте {@link #nominalDaysAvailable(ServiceInfo, LocalDate)} — поведение то же самое,
     *             приставка deprecated лишь отражает переименование для ясности.
     */
    @Deprecated(forRemoval = false)
    public static int daysAvailable(ServiceInfo serviceInfo, LocalDate date) {
        return nominalDaysAvailable(serviceInfo, date);
    }

    /**
     * Сколько дней из отпуска списано с календарного «пула»: {@code countOfDays - daysAddedByDeparture}.
     */
    public static int poolDaysDebited(Integer countOfDays, Integer daysAddedByDeparture) {
        int total = countOfDays == null ? 0 : countOfDays;
        int added = daysAddedByDeparture == null ? 0 : daysAddedByDeparture;
        return Math.max(0, total - added);
    }

    private static int tierDaysForSeniorityYears(int years) {
        if (years >= 15) {
            return DAYS_FROM_FIFTEEN;
        }
        if (years >= 10) {
            return DAYS_FROM_TEN_TO_FIFTEEN;
        }
        return DAYS_BELOW_TEN_YEAR_THRESHOLD;
    }

    private static int armyCreditYears(User user) {
        return user != null && user.getArmyService() == ArmyService.SERVED ? 1 : 0;
    }
}
