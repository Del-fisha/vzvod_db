package com.company.vzvod.job;

import com.company.vzvod.service.ServiceInfoVocationStatusService;
import io.jmix.core.security.Authenticated;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

public class ServiceInfoVocationStatusDailySyncJob implements Job {

    @Autowired
    private ServiceInfoVocationStatusService serviceInfoVocationStatusService;

    @Authenticated
    @Override
    public void execute(JobExecutionContext context) {
        serviceInfoVocationStatusService.syncAllForDate(LocalDate.now());
    }
}

