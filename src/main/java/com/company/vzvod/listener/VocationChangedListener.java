package com.company.vzvod.listener;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.Vocation;
import com.company.vzvod.service.ServiceInfoVocationStatusService;
import com.company.vzvod.service.VocationBalanceService;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

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
        if (event.getEntityId() == null) {
            return;
        }

        UUID serviceInfoId = resolveServiceInfoId(event);
        if (serviceInfoId == null) {
            return;
        }
        vocationBalanceService.recalcAndSave(serviceInfoId);
        serviceInfoVocationStatusService.syncForServiceInfo(serviceInfoId, LocalDate.now());
    }

    /**
     * При удалении запись уже может быть недоступна через загрузку — берём прежний FK из события.
     */
    private UUID resolveServiceInfoId(EntityChangedEvent<Vocation> event) {
        if (event.getType() == EntityChangedEvent.Type.DELETED) {
            Id<ServiceInfo> ref = event.getChanges().getOldReferenceId("userServiceInfo");
            if (ref == null) {
                return null;
            }
            Object raw = ref.getValue();
            return raw instanceof UUID u ? u : null;
        }

        Vocation vocation = dataManager.load(event.getEntityId()).one();
        ServiceInfo serviceInfo = vocation.getUserServiceInfo();
        return serviceInfo == null ? null : serviceInfo.getId();
    }
}
