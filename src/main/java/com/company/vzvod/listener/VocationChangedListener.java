package com.company.vzvod.listener;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.Vocation;
import com.company.vzvod.service.VocationBalanceService;
import com.company.vzvod.service.ServiceInfoVocationStatusService;
import io.jmix.core.DataManager;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class VocationChangedListener {

    private final DataManager dataManager;
    private final VocationBalanceService vocationBalanceService;
    private final ServiceInfoVocationStatusService serviceInfoVocationStatusService;

    public VocationChangedListener(
            DataManager dataManager,
            VocationBalanceService vocationBalanceService,
            ServiceInfoVocationStatusService serviceInfoVocationStatusService
    ) {
        this.dataManager = dataManager;
        this.vocationBalanceService = vocationBalanceService;
        this.serviceInfoVocationStatusService = serviceInfoVocationStatusService;
    }

    @EventListener
    public void onVocationChangedBeforeCommit(EntityChangedEvent<Vocation> event) {
        // Любая операция над отпуском должна пересчитать остаток дней за текущий год.
        // Event приходит before commit — даже при DELETE запись ещё можно загрузить в рамках транзакции.
        if (event.getEntityId() == null) {
            return;
        }

        Vocation vocation;
        try {
            vocation = dataManager.load(event.getEntityId()).one();
        } catch (Exception e) {
            return;
        }

        ServiceInfo serviceInfo = vocation.getUserServiceInfo();
        if (serviceInfo == null) {
            return;
        }

        vocationBalanceService.recalcAndSave(serviceInfo.getId());
        serviceInfoVocationStatusService.syncForServiceInfo(serviceInfo.getId(), LocalDate.now());
    }
}

