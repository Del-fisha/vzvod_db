package com.company.vzvod.listener;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.service.VocationService;
import io.jmix.core.DataManager;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ServiceInfoChangedListener {

    @Autowired
    private DataManager dataManager;

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
        recalcVacation(serviceInfo, LocalDate.now());
        dataManager.save(serviceInfo);
    }

    private void recalcVacation(ServiceInfo serviceInfo, LocalDate date) {
        if (serviceInfo.getUser() == null || serviceInfo.getStartDate() == null) {
            return;
        }
        int days = VocationService.daysAvailable(serviceInfo, date);
        serviceInfo.setVacationDaysEntitled(days);
        serviceInfo.setVacationDaysAvailable(days);
    }
}