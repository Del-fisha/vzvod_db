package com.company.vzvod.service;

import com.company.vzvod.entity.Vocation;
import io.jmix.core.event.EntitySavingEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VocationEntityEventsTest {

    @Test
    void onVocationSaving_setsCountOfDaysInclusive_andDefaultsAddedDays() {
        VocationEntityEvents events = new VocationEntityEvents();
        Vocation v = new Vocation();
        v.setStartDate(LocalDate.of(2026, 1, 1));
        v.setEndDate(LocalDate.of(2026, 1, 10));

        EntitySavingEvent<Vocation> ev = mock(EntitySavingEvent.class);
        when(ev.getEntity()).thenReturn(v);
        events.onVocationSaving(ev);

        assertEquals(10, v.getCountOfDays());
        assertNotNull(v.getDaysAddedByDeparture());
        assertEquals(0, v.getDaysAddedByDeparture());
    }
}

