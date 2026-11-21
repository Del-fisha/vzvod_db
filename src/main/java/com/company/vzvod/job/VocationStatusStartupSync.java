package com.company.vzvod.job;

import com.company.vzvod.service.ServiceInfoVocationStatusService;
import io.jmix.core.security.SystemAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Ночная сверка статусов уже стоит на 00:05 (Москва). Она не поможет человеку,
 * который уже в отпуске, пока не наступит следующая ночь и контейнер не пропустил запуск.
 * При старте прогоняем ту же сверку сразу.
 */
@Component
public class VocationStatusStartupSync {

    private static final Logger log = LoggerFactory.getLogger(VocationStatusStartupSync.class);

    private final ServiceInfoVocationStatusService serviceInfoVocationStatusService;
    private final SystemAuthenticator systemAuthenticator;

    public VocationStatusStartupSync(
            ServiceInfoVocationStatusService serviceInfoVocationStatusService,
            SystemAuthenticator systemAuthenticator
    ) {
        this.serviceInfoVocationStatusService = serviceInfoVocationStatusService;
        this.systemAuthenticator = systemAuthenticator;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void syncOnStartup() {
        LocalDate today = LocalDate.now();
        try {
            systemAuthenticator.runWithSystem(() -> serviceInfoVocationStatusService.syncAllForDate(today));
            log.info("Vocation statuses synced on startup for {}", today);
        } catch (RuntimeException ex) {
            log.error("Vocation status startup sync failed for {}", today, ex);
        }
    }
}
