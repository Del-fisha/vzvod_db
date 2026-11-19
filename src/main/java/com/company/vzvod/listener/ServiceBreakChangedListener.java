package com.company.vzvod.listener;

import com.company.vzvod.entity.ServiceBreak;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.service.VocationBalanceService;
import io.jmix.core.Id;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.event.EntityChangedEvent;
import io.jmix.core.security.SystemAuthenticator;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Перерыв стажа меняет чистую выслугу, поэтому котёл отпуска пересчитывается так же, как при смене даты начала службы.
 */
@Component
public class ServiceBreakChangedListener {

    private final UnconstrainedDataManager dataManager;
    private final VocationBalanceService vocationBalanceService;
    private final SystemAuthenticator systemAuthenticator;

    public ServiceBreakChangedListener(
            UnconstrainedDataManager dataManager,
            VocationBalanceService vocationBalanceService,
            SystemAuthenticator systemAuthenticator
    ) {
        this.dataManager = dataManager;
        this.vocationBalanceService = vocationBalanceService;
        this.systemAuthenticator = systemAuthenticator;
    }

    @EventListener
    public void onServiceBreakChangedBeforeCommit(EntityChangedEvent<ServiceBreak> event) {
        if (event.getEntityId() == null) {
            return;
        }
        systemAuthenticator.runWithSystem(() -> {
            UUID serviceInfoId = resolveServiceInfoId(event);
            if (serviceInfoId == null) {
                return;
            }
            vocationBalanceService.recalcAndSave(serviceInfoId);
        });
    }

    private UUID resolveServiceInfoId(EntityChangedEvent<ServiceBreak> event) {
        if (event.getType() == EntityChangedEvent.Type.DELETED) {
            Id<ServiceInfo> ref = event.getChanges().getOldReferenceId("serviceInfo");
            if (ref == null) {
                return null;
            }
            Object raw = ref.getValue();
            return raw instanceof UUID uuid ? uuid : null;
        }
        ServiceBreak serviceBreak = dataManager.load(event.getEntityId()).one();
        ServiceInfo serviceInfo = serviceBreak.getServiceInfo();
        return serviceInfo == null ? null : serviceInfo.getId();
    }
}
