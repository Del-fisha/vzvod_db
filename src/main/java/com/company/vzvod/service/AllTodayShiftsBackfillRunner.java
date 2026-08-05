package com.company.vzvod.service;

import io.jmix.core.security.SystemAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Однократный (идемпотентный) перенос уникальных дат смен в {@code ALL_TODAY_SHIFTS}.
 */
@Component
@Order(100)
public class AllTodayShiftsBackfillRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AllTodayShiftsBackfillRunner.class);

    private final AllTodayShiftsSyncService syncService;
    private final SystemAuthenticator systemAuthenticator;

    public AllTodayShiftsBackfillRunner(
            AllTodayShiftsSyncService syncService,
            SystemAuthenticator systemAuthenticator
    ) {
        this.syncService = syncService;
        this.systemAuthenticator = systemAuthenticator;
    }

    @Override
    public void run(ApplicationArguments args) {
        systemAuthenticator.begin();
        try {
            int created = syncService.syncFromShifts();
            if (created > 0) {
                log.info("AllTodayShifts backfill: created {} row(s)", created);
            }
        } finally {
            systemAuthenticator.end();
        }
    }
}
