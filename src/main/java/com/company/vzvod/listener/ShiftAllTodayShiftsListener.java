package com.company.vzvod.listener;

import com.company.vzvod.entity.Shift;
import com.company.vzvod.service.AllTodayShiftsSyncService;
import io.jmix.core.event.EntitySavingEvent;
import io.jmix.core.security.SystemAuthenticator;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * При сохранении Shift гарантирует строку в {@link com.company.vzvod.entity.AllTodayShifts}.
 * Вызов под system auth: мобильный/бот API не ставят UserDetails в SecurityContext,
 * а DataManager через AccessLogger падает с «Authentication principal must be UserDetails».
 */
@Component
public class ShiftAllTodayShiftsListener {

    private final AllTodayShiftsSyncService syncService;
    private final SystemAuthenticator systemAuthenticator;

    public ShiftAllTodayShiftsListener(
            AllTodayShiftsSyncService syncService,
            SystemAuthenticator systemAuthenticator
    ) {
        this.syncService = syncService;
        this.systemAuthenticator = systemAuthenticator;
    }

    @EventListener
    public void onShiftSaving(EntitySavingEvent<Shift> event) {
        Shift shift = event.getEntity();
        if (shift == null) {
            return;
        }
        systemAuthenticator.runWithSystem(() ->
                syncService.ensureExists(shift.getDate(), shift.getDepartmentToday()));
    }
}
