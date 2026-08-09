package com.company.vzvod.service;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.VocationType;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class VocationBalanceService {

    private final DataManager dataManager;

    public VocationBalanceService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /** Типы, которые списывают/добавляют дни в {@link ServiceInfo#getVacationDaysEntitled()}/{@link ServiceInfo#getVacationDaysAvailable()}. */
    static List<Integer> balanceAffectingTypeIds() {
        return List.of(
                VocationType.MAIN.getId(),
                VocationType.ADDITIONAL.getId(),
                VocationType.PART_OF_MAIN.getId()
        );
    }

    /**
     * Пересчитывает положено/остаток отпуска за календарный год вокруг «сегодня».
     * Номинал на год = норматив по выслуге (месяцы) на 01.01 + доп. +5 за пороги 120/180/240 мес. в течение года
     * (если на 01.01 порог ещё не пройден) + сумма «добавлено дней» по выезду.
     * Списание — только с пула: {@code countOfDays - daysAddedByDeparture}
     * по основному, дополнительному и части основного.
     */
    public void recalcAndSave(ServiceInfo serviceInfo) {
        if (serviceInfo == null || serviceInfo.getId() == null) {
            return;
        }
        recalcAndSave(serviceInfo.getId());
    }

    public VocationYearStats recalcAndSave(UUID serviceInfoId) {
        if (serviceInfoId == null) {
            return new VocationYearStats(0, 0, 0);
        }

        ServiceInfo serviceInfo = loadForVacationCalc(serviceInfoId);
        if (serviceInfo == null) {
            return new VocationYearStats(0, 0, 0);
        }
        VocationYearStats stats = calcCurrentYearStats(serviceInfo, LocalDate.now());
        serviceInfo.setVacationDaysEntitled(stats.entitled());
        serviceInfo.setVacationDaysAvailable(stats.available());
        dataManager.save(serviceInfo);
        return stats;
    }

    public VocationYearStats calcCurrentYearStats(ServiceInfo serviceInfo, LocalDate now) {
        if (serviceInfo == null) {
            return new VocationYearStats(0, 0, 0);
        }
        // Если дата начала службы не задана, используем базовый лимит 40,
        // но всё равно учитываем уже созданные отпуска/добавленные дни в текущем году.
        if (serviceInfo.getStartDate() == null) {
            if (serviceInfo.getId() == null) {
                return new VocationYearStats(40, 0, 40);
            }
            LocalDate yearStart = LocalDate.of(now.getYear(), 1, 1);
            LocalDate yearEnd = LocalDate.of(now.getYear(), 12, 31);
            int added = loadAddedDays(serviceInfo.getId(), yearStart, yearEnd);
            int entitled = 40 + Math.max(0, added);
            int used = loadPoolDaysUsed(serviceInfo.getId(), yearStart, yearEnd);
            int available = Math.max(0, entitled - used);
            return new VocationYearStats(entitled, used, available);
        }
        if (serviceInfo.getUser() == null) {
            // без пользователя не можем учесть армию/стаж — считаем как минимум 40,
            // но по возможности учитываем фактические отпуска в этом году
            if (serviceInfo.getId() == null) {
                return new VocationYearStats(40, 0, 40);
            }
            LocalDate yearStart = LocalDate.of(now.getYear(), 1, 1);
            LocalDate yearEnd = LocalDate.of(now.getYear(), 12, 31);
            int added = loadAddedDays(serviceInfo.getId(), yearStart, yearEnd);
            int entitled = 40 + Math.max(0, added);
            int used = loadPoolDaysUsed(serviceInfo.getId(), yearStart, yearEnd);
            int available = Math.max(0, entitled - used);
            return new VocationYearStats(entitled, used, available);
        }

        LocalDate yearStart = LocalDate.of(now.getYear(), 1, 1);
        LocalDate yearEnd = LocalDate.of(now.getYear(), 12, 31);

        int nominalAtJan1 = VocationService.nominalDaysAvailable(serviceInfo, yearStart);
        int midYearBonuses = VocationService.midYearSeniorityBonuses(serviceInfo, yearStart, now);
        int added = loadAddedDays(serviceInfo.getId(), yearStart, yearEnd);
        int entitled = nominalAtJan1 + midYearBonuses + Math.max(0, added);

        int used = loadPoolDaysUsed(serviceInfo.getId(), yearStart, yearEnd);
        int available = Math.max(0, entitled - used);

        return new VocationYearStats(entitled, used, available);
    }

    /**
     * Тот же расчёт, что и у {@link #calcCurrentYearStats(ServiceInfo, LocalDate)}, без записи в {@link ServiceInfo}.
     * Используется в UI редактора отпуска до сохранения.
     */
    public VocationYearStats calcCurrentYearStats(UUID serviceInfoId, LocalDate now) {
        if (serviceInfoId == null) {
            return new VocationYearStats(0, 0, 0);
        }
        ServiceInfo serviceInfo = loadForVacationCalc(serviceInfoId);
        if (serviceInfo == null) {
            return new VocationYearStats(0, 0, 0);
        }
        return calcCurrentYearStats(serviceInfo, now);
    }

    private int loadPoolDaysUsed(UUID serviceInfoId, LocalDate start, LocalDate end) {
        if (serviceInfoId == null) {
            return 0;
        }

        int sumCounted = vocationSumCountedDays(serviceInfoId, start, end);
        int sumAddedParts = loadAddedDays(serviceInfoId, start, end);
        return Math.max(0, sumCounted - sumAddedParts);
    }

    /** Сумма {@code countOfDays} только по тем же типам, что учитываются в балансе. */
    private int vocationSumCountedDays(UUID serviceInfoId, LocalDate start, LocalDate end) {
        Integer sum = dataManager.loadValue(
                        "select sum(coalesce(e.countOfDays, 0)) " +
                                "from Vocation e " +
                                "where e.userServiceInfo.id = :serviceInfoId " +
                                "  and e.startDate >= :start and e.startDate <= :end " +
                                "  and e.typeId in :types",
                        Integer.class
                )
                .parameter("serviceInfoId", serviceInfoId)
                .parameter("start", start)
                .parameter("end", end)
                .parameter("types", balanceAffectingTypeIds())
                .one();

        return sum == null ? 0 : sum;
    }

    private int loadAddedDays(UUID serviceInfoId, LocalDate start, LocalDate end) {
        if (serviceInfoId == null) {
            return 0;
        }

        Integer sum = dataManager.loadValue(
                        "select sum(coalesce(e.daysAddedByDeparture, 0)) " +
                                "from Vocation e " +
                                "where e.userServiceInfo.id = :serviceInfoId " +
                                "  and e.startDate >= :start and e.startDate <= :end " +
                                "  and e.typeId in :types",
                        Integer.class
                )
                .parameter("serviceInfoId", serviceInfoId)
                .parameter("start", start)
                .parameter("end", end)
                .parameter("types", balanceAffectingTypeIds())
                .one();

        return sum == null ? 0 : sum;
    }

    private ServiceInfo loadForVacationCalc(UUID serviceInfoId) {
        return dataManager.load(ServiceInfo.class)
                .id(serviceInfoId)
                .fetchPlan(fp -> fp
                        .add("startDate")
                        .add("monthsOfServiceBeforeLastAppointment")
                        .add("status")
                        .add("vacationDaysEntitled")
                        .add("vacationDaysAvailable")
                        .add("user", fp2 -> fp2.add("armyService"))
                )
                .optional()
                .orElse(null);
    }

    public record VocationYearStats(int entitled, int used, int available) {
    }
}
