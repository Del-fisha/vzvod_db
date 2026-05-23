package com.company.vzvod.service;

import com.company.vzvod.entity.Penalty;
import io.jmix.core.event.EntitySavingEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PenaltyEntityEvents {

    @EventListener
    public void onPenaltySaving(EntitySavingEvent<Penalty> event) {
        event.getEntity().autoCompleteIfExpired(LocalDate.now());
    }
}
