package com.company.vzvod.service;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.StatusInService;
import io.jmix.core.event.EntitySavingEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ServiceInfoEntityEvents {

    private final ServiceInfoVocationStatusService serviceInfoVocationStatusService;

    public ServiceInfoEntityEvents(ServiceInfoVocationStatusService serviceInfoVocationStatusService) {
        this.serviceInfoVocationStatusService = serviceInfoVocationStatusService;
    }

    @EventListener
    public void onServiceInfoSaving(EntitySavingEvent<ServiceInfo> event) {
        ServiceInfo serviceInfo = event.getEntity();
        if (serviceInfo.getStatus() == null) {
            serviceInfo.setStatus(StatusInService.ACTIVE);
        }

        StatusInService status = serviceInfo.getStatus();
        if (status != StatusInService.ACTIVE && status != StatusInService.VOCATION) {
            return;
        }

        // Перед сохранением аккуратно приводим статус к "сегодняшнему" отпуску.
        // Для новой сущности ID ещё нет — тогда синхронизацию сделает listener по отпуску/джоба.
        if (serviceInfo.getId() == null) {
            return;
        }

        boolean hasVocationToday = serviceInfoVocationStatusService.hasVocationToday(serviceInfo.getId(), LocalDate.now());
        serviceInfo.setStatus(hasVocationToday ? StatusInService.VOCATION : StatusInService.ACTIVE);
    }
}