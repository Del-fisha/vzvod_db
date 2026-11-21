package com.company.vzvod.service;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.VocationType;
import io.jmix.core.UnconstrainedDataManager;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class VocationBalanceService {

    private final UnconstrainedDataManager dataManager;

    public VocationBalanceService(UnconstrainedDataManager dataManager) {
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
     * Положено = норматив по выслуге на 01.01 + 5 дней за порог 10/15/20 лет, если он наступает в этом году.
     * «Добавлено дней», учебный отпуск и ЦПП этот лимит не меняют.
     * Списание — {@code countOfDays} основного, дополнительного и части основного,
     * отпуск целиком относится к году даты начала.
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
        // Без даты начала службы или пользователя выслугу не посчитать: котёл 40 дней.
        // Уже созданные отпуска этого года всё равно списываются.
        if (serviceInfo.getStartDate() == null || serviceInfo.getUser() == null) {
            if (serviceInfo.getId() == null) {
                return new VocationYearStats(40, 0, 40);
            }
            LocalDate yearStart = LocalDate.of(now.getYear(), 1, 1);
            LocalDate yearEnd = LocalDate.of(now.getYear(), 12, 31);
            int entitled = 40;
            int used = loadPoolDaysUsed(serviceInfo.getId(), yearStart, yearEnd);
            int available = Math.max(0, entitled - used);
            return new VocationYearStats(entitled, used, available);
        }

        LocalDate yearStart = LocalDate.of(now.getYear(), 1, 1);
        LocalDate yearEnd = LocalDate.of(now.getYear(), 12, 31);

        int nominalAtJan1 = VocationService.nominalDaysAvailable(serviceInfo, yearStart);
        int midYearBonuses = VocationService.midYearSeniorityBonuses(serviceInfo, yearStart, now);
        int entitled = nominalAtJan1 + midYearBonuses;

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
        return vocationSumCountedDays(serviceInfoId, start, end);
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

    private ServiceInfo loadForVacationCalc(UUID serviceInfoId) {
        return dataManager.load(ServiceInfo.class)
                .id(serviceInfoId)
                .fetchPlan(fp -> fp
                        .add("startDate")
                        .add("serviceBreaks", breaks -> breaks.add("fromDate").add("toDate"))
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
