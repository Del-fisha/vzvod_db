package com.company.vzvod.service;

import com.company.vzvod.entity.ArmyService;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Норматив основного отпуска по общей выслуге в месяцах:
 * <ul>
 *     <li>месяцы от {@link ServiceInfo#getStartDate()} до даты расчёта;</li>
 *     <li>плюс {@link ServiceInfo#getMonthsOfServiceBeforeLastAppointment()};</li>
 *     <li>плюс 12 месяцев при {@link User#getArmyService()} {@code == SERVED}.</li>
 * </ul>
 * Пороги отпуска: 120 / 180 / 240 месяцев (10 / 15 / 20 лет). Для отображения пользователю
 * используйте {@link #effectiveYears(ServiceInfo, LocalDate)} — полные годы (целочисленное деление).
 */
public final class VocationService {

    private static final int MONTHS_PER_YEAR = 12;
    private static final int MONTHS_TEN_YEARS = 10 * MONTHS_PER_YEAR;
    private static final int MONTHS_FIFTEEN_YEARS = 15 * MONTHS_PER_YEAR;
    private static final int MONTHS_TWENTY_YEARS = 20 * MONTHS_PER_YEAR;
    private static final int ARMY_CREDIT_MONTHS = MONTHS_PER_YEAR;

    private static final int DAYS_BELOW_TEN_YEAR_THRESHOLD = 40;
    private static final int DAYS_FROM_TEN_TO_FIFTEEN = 45;
    private static final int DAYS_FROM_FIFTEEN_TO_NINETEEN = 50;
    private static final int DAYS_FROM_TWENTY = 55;
    /** Дополнительно к доступному после порога 120 / 180 / 240 месяцев внутри календарного года. */
    static final int MID_YEAR_TIER_DELTA = 5;

    private VocationService() {
    }

    /**
     * Общая выслуга в месяцах на дату {@code onDate}.
     */
    public static int effectiveMonths(ServiceInfo serviceInfo, LocalDate onDate) {
        if (serviceInfo == null || serviceInfo.getStartDate() == null || onDate == null) {
            return 0;
        }
        LocalDate startDate = serviceInfo.getStartDate();
        int monthsAtCurrentPost = onDate.isBefore(startDate)
                ? 0
                : (int) ChronoUnit.MONTHS.between(startDate, onDate);
        return monthsAtCurrentPost + priorServiceMonths(serviceInfo) + armyCreditMonths(serviceInfo.getUser());
    }

    /**
     * Полные годы выслуги для отображения (без дробной части месяцев).
     */
    public static int effectiveYears(ServiceInfo serviceInfo, LocalDate onDate) {
        return effectiveMonths(serviceInfo, onDate) / MONTHS_PER_YEAR;
    }

    public static int nominalDaysAvailable(ServiceInfo serviceInfo, LocalDate date) {
        int months = effectiveMonths(serviceInfo, date);
        return tierDaysForSeniorityMonths(months);
    }

    /**
     * Сумма +{@value #MID_YEAR_TIER_DELTA} за каждый порог (120, 180, 240 месяцев), который:
     * <ul>
     *     <li>ещё не был достигнут на {@code yearStartInclusive};</li>
     *     <li>достигнут не позже {@code evaluationDateInclusive};</li>
     *     <li>относится к календарному году с началом {@code yearStartInclusive}.</li>
     * </ul>
     */
    public static int midYearSeniorityBonuses(ServiceInfo serviceInfo,
                                              LocalDate yearStartInclusive,
                                              LocalDate evaluationDateInclusive) {
        if (serviceInfo == null || serviceInfo.getStartDate() == null
                || yearStartInclusive == null || evaluationDateInclusive == null) {
            return 0;
        }
        LocalDate until = evaluationDateInclusive.isBefore(yearStartInclusive)
                ? yearStartInclusive.minusDays(1)
                : evaluationDateInclusive;

        int monthsAtJan1 = effectiveMonths(serviceInfo, yearStartInclusive);
        int monthsAtUntil = effectiveMonths(serviceInfo, until);

        int bonus = 0;
        if (monthsAtJan1 < MONTHS_TEN_YEARS && monthsAtUntil >= MONTHS_TEN_YEARS) {
            bonus += MID_YEAR_TIER_DELTA;
        }
        if (monthsAtJan1 < MONTHS_FIFTEEN_YEARS && monthsAtUntil >= MONTHS_FIFTEEN_YEARS) {
            bonus += MID_YEAR_TIER_DELTA;
        }
        if (monthsAtJan1 < MONTHS_TWENTY_YEARS && monthsAtUntil >= MONTHS_TWENTY_YEARS) {
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

    public static int poolDaysDebited(Integer countOfDays, Integer daysAddedByDeparture) {
        int total = countOfDays == null ? 0 : countOfDays;
        int added = daysAddedByDeparture == null ? 0 : daysAddedByDeparture;
        return Math.max(0, total - added);
    }

    private static int tierDaysForSeniorityMonths(int months) {
        if (months >= MONTHS_TWENTY_YEARS) {
            return DAYS_FROM_TWENTY;
        }
        if (months >= MONTHS_FIFTEEN_YEARS) {
            return DAYS_FROM_FIFTEEN_TO_NINETEEN;
        }
        if (months >= MONTHS_TEN_YEARS) {
            return DAYS_FROM_TEN_TO_FIFTEEN;
        }
        return DAYS_BELOW_TEN_YEAR_THRESHOLD;
    }

    private static int armyCreditMonths(User user) {
        return user != null && user.getArmyService() == ArmyService.SERVED ? ARMY_CREDIT_MONTHS : 0;
    }

    private static int priorServiceMonths(ServiceInfo serviceInfo) {
        Integer months = serviceInfo.getMonthsOfServiceBeforeLastAppointment();
        return months == null ? 0 : Math.max(0, months);
    }
}
