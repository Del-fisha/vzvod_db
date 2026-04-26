package com.company.vzvod.service;

import com.company.vzvod.entity.Vocation;
import io.jmix.core.event.EntitySavingEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class VocationEntityEvents {

    @EventListener
    public void onVocationSaving(EntitySavingEvent<Vocation> event) {
        Vocation vocation = event.getEntity();
        LocalDate start = vocation.getStartDate();
        LocalDate end = vocation.getEndDate();
        if (start == null || end == null) {
            return;
        }

        long days = ChronoUnit.DAYS.between(start, end) + 1; // inclusive
        if (days < 0) {
            days = 0;
        }
        vocation.setCountOfDays((int) days);
        if (vocation.getDaysAddedByDeparture() == null) {
            vocation.setDaysAddedByDeparture(0);
        }
    }
}

