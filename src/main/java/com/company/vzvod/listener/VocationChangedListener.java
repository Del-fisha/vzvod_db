package com.company.vzvod.listener;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.Vocation;
import com.company.vzvod.service.ServiceInfoVocationStatusService;
import com.company.vzvod.service.VocationBalanceService;
import io.jmix.core.Id;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.event.EntityChangedEvent;
import io.jmix.core.security.SystemAuthenticator;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Пересчёт баланса/статуса отпуска.
 * Мобильный/бот API не ставят UserDetails — используем UnconstrainedDataManager
 * (+ system auth на случай вложенных слушателей с constrained DataManager).
 */
@Component
public class VocationChangedListener {

    private final UnconstrainedDataManager dataManager;
    private final VocationBalanceService vocationBalanceService;
    private final ServiceInfoVocationStatusService serviceInfoVocationStatusService;
    private final SystemAuthenticator systemAuthenticator;

    public VocationChangedListener(
            UnconstrainedDataManager dataManager,
            VocationBalanceService vocationBalanceService,
            ServiceInfoVocationStatusService serviceInfoVocationStatusService,
            SystemAuthenticator systemAuthenticator
    ) {
        this.dataManager = dataManager;
        this.vocationBalanceService = vocationBalanceService;
        this.serviceInfoVocationStatusService = serviceInfoVocationStatusService;
        this.systemAuthenticator = systemAuthenticator;
    }

    @EventListener
    public void onVocationChangedBeforeCommit(EntityChangedEvent<Vocation> event) {
        if (event.getEntityId() == null) {
            return;
        }

        systemAuthenticator.runWithSystem(() -> {
            UUID serviceInfoId = resolveServiceInfoId(event);
            if (serviceInfoId == null) {
                return;
            }
            vocationBalanceService.recalcAndSave(serviceInfoId);
            serviceInfoVocationStatusService.syncForServiceInfo(serviceInfoId, LocalDate.now());
        });
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
