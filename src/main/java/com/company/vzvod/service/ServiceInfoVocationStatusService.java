package com.company.vzvod.service;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.StatusInService;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ServiceInfoVocationStatusService {

    private final DataManager dataManager;

    public ServiceInfoVocationStatusService(DataManager dataManager) {
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
     * Синхронизирует статус только для одного сотрудника.
     * Делает один лёгкий запрос в БД (проверка отпуска на дату).
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
        if (status != StatusInService.ACTIVE && status != StatusInService.VOCATION) {
            return;
        }

        boolean hasVocationToday = hasVocationToday(serviceInfoId, today);
        StatusInService expected = hasVocationToday ? StatusInService.VOCATION : StatusInService.ACTIVE;

        if (status != expected) {
            serviceInfo.setStatus(expected);
            dataManager.save(serviceInfo);
        }
    }

    /**
     * Синхронизирует статусы массово на дату:
     * - ACTIVE -> VOCATION если отпуск "накрывает" today
     * - VOCATION -> ACTIVE если такого отпуска нет
     *
     * Нагрузка на БД минимальная: 2 запроса на список id + обновление только изменившихся.
     */
    public void syncAllForDate(LocalDate today) {
        if (today == null) {
            return;
        }

        List<UUID> toVocation = dataManager.loadValue(
                        "select si.id " +
                                "from ServiceInfo si " +
                                "where si.status = :active " +
                                "  and exists (" +
                                "    select 1 from Vocation v " +
                                "    where v.userServiceInfo = si " +
                                "      and v.startDate <= :today " +
                                "      and v.endDate >= :today" +
                                "  )",
                        UUID.class
                )
                .parameter("active", StatusInService.ACTIVE.getId())
                .parameter("today", today)
                .list();

        List<UUID> toActive = dataManager.loadValue(
                        "select si.id " +
                                "from ServiceInfo si " +
                                "where si.status = :vocation " +
                                "  and not exists (" +
                                "    select 1 from Vocation v " +
                                "    where v.userServiceInfo = si " +
                                "      and v.startDate <= :today " +
                                "      and v.endDate >= :today" +
                                "  )",
                        UUID.class
                )
                .parameter("vocation", StatusInService.VOCATION.getId())
                .parameter("today", today)
                .list();

        if (!toVocation.isEmpty()) {
            List<ServiceInfo> items = dataManager.load(ServiceInfo.class)
                    .query("select si from ServiceInfo si where si.id in :ids")
                    .parameter("ids", toVocation)
                    .fetchPlan(fp -> fp.add("status"))
                    .list();
            for (ServiceInfo si : items) {
                si.setStatus(StatusInService.VOCATION);
            }
            dataManager.save(items);
        }

        if (!toActive.isEmpty()) {
            List<ServiceInfo> items = dataManager.load(ServiceInfo.class)
                    .query("select si from ServiceInfo si where si.id in :ids")
                    .parameter("ids", toActive)
                    .fetchPlan(fp -> fp.add("status"))
                    .list();
            for (ServiceInfo si : items) {
                si.setStatus(StatusInService.ACTIVE);
            }
            dataManager.save(items);
        }
    }
}

