package com.company.vzvod.service;

import com.company.vzvod.entity.Vehicle;
import io.jmix.core.event.EntitySavingEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class VehicleEntityEvents {

    private final VehicleStateNumberService vehicleStateNumberService;

    public VehicleEntityEvents(VehicleStateNumberService vehicleStateNumberService) {
        this.vehicleStateNumberService = vehicleStateNumberService;
    }

    @EventListener
    public void onVehicleSaving(EntitySavingEvent<Vehicle> event) {
        Vehicle v = event.getEntity();
        String raw = v.getStateNumber();
        if (raw == null || raw.isBlank()) {
            return;
        }
        // Normalize before saving to DB (UI and DataManager).
        v.setStateNumber(vehicleStateNumberService.normalize(raw));
    }
}

