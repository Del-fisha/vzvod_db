package com.company.vzvod.service;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import com.company.vzvod.entity.Vocation;
import com.company.vzvod.entity.VocationType;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
public class VocationBalanceService {

    private final DataManager dataManager;

    public VocationBalanceService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Пересчитывает лимит/остаток отпускных дней за текущий календарный год.
     * Лимит фиксируется на 01.01 текущего года (т.е. увеличение стажа в середине года не повышает лимит до следующего 01.01).
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
        VocationYearStats stats = calcCurrentYearStats(serviceInfo, LocalDate.now());
        serviceInfo.setVacationDaysEntitled(stats.entitled());
        serviceInfo.setVacationDaysAvailable(stats.available());
        dataManager.save(serviceInfo);
        return stats;
    }

    public VocationYearStats calcCurrentYearStats(ServiceInfo serviceInfo, LocalDate now) {
        if (serviceInfo == null || serviceInfo.getUser() == null || serviceInfo.getStartDate() == null) {
            return new VocationYearStats(0, 0, 0);
        }

        LocalDate yearStart = LocalDate.of(now.getYear(), 1, 1);
        LocalDate yearEnd = LocalDate.of(now.getYear(), 12, 31);

        int entitledBase = VocationService.daysAvailable(serviceInfo, yearStart);
        int used = loadUsedDays(serviceInfo.getId(), yearStart, yearEnd);
        int added = loadAddedDays(serviceInfo.getId(), yearStart, yearEnd);
        int entitled = entitledBase + Math.max(0, added);
        int available = Math.max(0, entitled - used);

        return new VocationYearStats(entitled, used, available);
    }

    private int loadUsedDays(UUID serviceInfoId, LocalDate start, LocalDate end) {
        if (serviceInfoId == null) {
            return 0;
        }

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
                .parameter("types", List.of(VocationType.MAIN.getId(), VocationType.PART_OF_MAIN.getId()))
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
                .parameter("types", List.of(VocationType.MAIN.getId(), VocationType.PART_OF_MAIN.getId()))
                .one();

        return sum == null ? 0 : sum;
    }

    private ServiceInfo loadForVacationCalc(UUID serviceInfoId) {
        return dataManager.load(ServiceInfo.class)
                .id(serviceInfoId)
                .fetchPlan(fp -> fp
                        .add("startDate")
                        .add("status")
                        .add("vacationDaysEntitled")
                        .add("vacationDaysAvailable")
                        .add("user", fp2 -> fp2.add("armyService"))
                )
                .one();
    }

    public record VocationYearStats(int entitled, int used, int available) {
    }
}

