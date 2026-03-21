package com.company.vzvod.config;

import com.company.vzvod.job.ServiceInfoYearlyRecalcJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.CronScheduleBuilder.cronSchedule;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail serviceInfoYearlyRecalcJobDetail() {
        return newJob(ServiceInfoYearlyRecalcJob.class)
                .withIdentity("serviceInfoYearlyRecalc")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger serviceInfoYearlyRecalcTrigger(JobDetail serviceInfoYearlyRecalcJobDetail) {
        return newTrigger()
                .forJob(serviceInfoYearlyRecalcJobDetail)
                .withIdentity("serviceInfoYearlyRecalcTrigger")
                .withSchedule(
                        cronSchedule("0 0 1 1 1 ?")
                                .inTimeZone(TimeZone.getTimeZone("Europe/Moscow"))
                )
                .build();
    }
}