package com.company.vzvod.listener;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.service.VocationBalanceService;
import io.jmix.core.DataManager;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ServiceInfoChangedListener {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private VocationBalanceService vocationBalanceService;

    @EventListener
    public void onServiceInfoChangedBeforeCommit(EntityChangedEvent<ServiceInfo> event) {
        if (event.getType() == EntityChangedEvent.Type.DELETED) {
            return;
        }

        boolean needRecalc =
                event.getType() == EntityChangedEvent.Type.CREATED
                        || event.getChanges().isChanged("startDate");

        if (!needRecalc) {
            return;
        }

        ServiceInfo serviceInfo = dataManager.load(event.getEntityId()).one();
        vocationBalanceService.recalcAndSave(serviceInfo.getId());
    }
}