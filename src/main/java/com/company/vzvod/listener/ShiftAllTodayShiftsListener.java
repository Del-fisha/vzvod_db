package com.company.vzvod.listener;

import com.company.vzvod.entity.Shift;
import com.company.vzvod.service.AllTodayShiftsSyncService;
import io.jmix.core.event.EntitySavingEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * При сохранении Shift гарантирует строку в {@link com.company.vzvod.entity.AllTodayShifts}.
 */
@Component
public class ShiftAllTodayShiftsListener {

    private final AllTodayShiftsSyncService syncService;

    public ShiftAllTodayShiftsListener(AllTodayShiftsSyncService syncService) {
        this.syncService = syncService;
    }

    @EventListener
    public void onShiftSaving(EntitySavingEvent<Shift> event) {
        Shift shift = event.getEntity();
        if (shift == null) {
            return;
        }
        syncService.ensureExists(shift.getDate(), shift.getDepartmentToday());
    }
}
