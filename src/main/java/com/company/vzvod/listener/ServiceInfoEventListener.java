package com.company.vzvod.listener;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.service.VocationService;
import io.jmix.core.event.EntitySavingEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ServiceInfoEventListener {

    @EventListener
    public void onServiceInfoSaving(EntitySavingEvent<ServiceInfo> event) {
        ServiceInfo serviceInfo = event.getEntity();

        if (event.isNewEntity()) {
            recalcVacation(serviceInfo, LocalDate.now());
        }
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