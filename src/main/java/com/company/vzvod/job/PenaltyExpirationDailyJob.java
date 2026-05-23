package com.company.vzvod.job;

import com.company.vzvod.service.PenaltyExpirationService;
import io.jmix.core.security.Authenticated;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

public class PenaltyExpirationDailyJob implements Job {

    @Autowired
    private PenaltyExpirationService penaltyExpirationService;

    @Authenticated
    @Override
    public void execute(JobExecutionContext context) {
        penaltyExpirationService.completeAllExpired(LocalDate.now());
    }
}
