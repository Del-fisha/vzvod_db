package com.company.vzvod.service;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.StatusInService;
import com.company.vzvod.entity.Vocation;
import com.company.vzvod.entity.VocationType;
import io.jmix.core.UnconstrainedDataManager;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ServiceInfoVocationStatusService {

    private static final List<Integer> MANAGED_STATUS_IDS = List.of(
            StatusInService.ACTIVE.getId(),
            StatusInService.VOCATION.getId(),
            StatusInService.STUDY_LEAVE.getId(),
            StatusInService.PTC.getId()
    );

    private final UnconstrainedDataManager dataManager;

    public ServiceInfoVocationStatusService(UnconstrainedDataManager dataManager) {
        this.dataManager = dataManager;
    }

    public boolean hasVocationToday(UUID serviceInfoId, LocalDate today) {
        if (serviceInfoId == null || today == null) {
            return false;
        }

        Long cnt = dataManager.loadValue(
                        "select count(v) " +
                                "from Vocation v " +
                                "where v.userServiceInfo.id = :serviceInfoId " +
                                "  and v.startDate <= :today " +
                                "  and v.endDate >= :today",
                        Long.class
                )
                .parameter("serviceInfoId", serviceInfoId)
                .parameter("today", today)
                .one();

        return cnt != null && cnt > 0;
    }

    /**
     * Статус по типам отпусков, накрывающих дату.
     * Приоритет: Цпп > учебный отпуск > обычный отпуск > в строю.
     */
    static StatusInService resolveFromTypeIds(Collection<Integer> typeIds) {
        if (typeIds == null || typeIds.isEmpty()) {
            return StatusInService.ACTIVE;
        }

        boolean hasPtc = false;
        boolean hasStudyLeave = false;
        boolean hasRegular = false;

        for (Integer typeId : typeIds) {
            VocationType type = VocationType.fromId(typeId);
            if (type == VocationType.PTC) {
                hasPtc = true;
            } else if (type == VocationType.STUDY_LEAVE) {
                hasStudyLeave = true;
            } else if (type != null) {
                hasRegular = true;
            }
        }

        if (hasPtc) {
            return StatusInService.PTC;
        }
        if (hasStudyLeave) {
            return StatusInService.STUDY_LEAVE;
        }
        if (hasRegular) {
            return StatusInService.VOCATION;
        }
        return StatusInService.ACTIVE;
    }

    static boolean isManagedStatus(StatusInService status) {
        return status == StatusInService.ACTIVE
                || status == StatusInService.VOCATION
                || status == StatusInService.STUDY_LEAVE
                || status == StatusInService.PTC;
    }

    public StatusInService resolveExpectedStatus(UUID serviceInfoId, LocalDate today) {
        if (serviceInfoId == null || today == null) {
            return StatusInService.ACTIVE;
        }
        List<Integer> typeIds = dataManager.loadValue(
                        "select v.typeId " +
                                "from Vocation v " +
                                "where v.userServiceInfo.id = :serviceInfoId " +
                                "  and v.startDate <= :today " +
                                "  and v.endDate >= :today",
                        Integer.class
                )
                .parameter("serviceInfoId", serviceInfoId)
                .parameter("today", today)
                .list();
        return resolveFromTypeIds(typeIds);
    }

    /**
     * Синхронизирует статус только для одного сотрудника.
     * Делает один лёгкий запрос в БД (типы отпусков на дату).
     */
    public void syncForServiceInfo(UUID serviceInfoId, LocalDate today) {
        if (serviceInfoId == null || today == null) {
            return;
        }

        ServiceInfo serviceInfo = dataManager.load(ServiceInfo.class)
                .id(serviceInfoId)
                .fetchPlan(fp -> fp.add("status"))
                .optional()
                .orElse(null);
        if (serviceInfo == null) {
            return;
        }

        StatusInService status = serviceInfo.getStatus();
        if (!isManagedStatus(status)) {
            return;
        }

        StatusInService expected = resolveExpectedStatus(serviceInfoId, today);
        if (status != expected) {
            serviceInfo.setStatus(expected);
            dataManager.save(serviceInfo);
        }
    }

    /**
     * Синхронизирует статусы массово на дату среди управляемых:
     * ACTIVE / VOCATION / STUDY_LEAVE / PTC.
     */
    public void syncAllForDate(LocalDate today) {
        if (today == null) {
            return;
        }

        List<ServiceInfo> candidates = dataManager.load(ServiceInfo.class)
                .query("select si from ServiceInfo si where si.status in :managed")
                .parameter("managed", MANAGED_STATUS_IDS)
                .fetchPlan(fp -> fp.add("status"))
                .list();
        if (candidates.isEmpty()) {
            return;
        }

        List<UUID> ids = new ArrayList<>(candidates.size());
        for (ServiceInfo si : candidates) {
            ids.add(si.getId());
        }

        List<Vocation> covering = dataManager.load(Vocation.class)
                .query("select v from Vocation v " +
                        "where v.userServiceInfo.id in :ids " +
                        "  and v.startDate <= :today " +
                        "  and v.endDate >= :today")
                .parameter("ids", ids)
                .parameter("today", today)
                .fetchPlan(fp -> fp
                        .add("typeId")
                        .add("userServiceInfo", siFp -> siFp.add("id")))
                .list();

        Map<UUID, Set<Integer>> typesByServiceInfo = new HashMap<>();
        for (Vocation v : covering) {
            if (v.getUserServiceInfo() == null || v.getUserServiceInfo().getId() == null) {
                continue;
            }
            typesByServiceInfo
                    .computeIfAbsent(v.getUserServiceInfo().getId(), ignored -> new HashSet<>())
                    .add(v.getTypeId());
        }

        List<ServiceInfo> toSave = new ArrayList<>();
        for (ServiceInfo si : candidates) {
            StatusInService expected = resolveFromTypeIds(
                    typesByServiceInfo.getOrDefault(si.getId(), Set.of())
            );
            if (si.getStatus() != expected) {
                si.setStatus(expected);
                toSave.add(si);
            }
        }

        if (!toSave.isEmpty()) {
            dataManager.save(toSave);
        }
    }
}
