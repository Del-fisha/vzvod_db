package com.company.vzvod.config;

import com.company.vzvod.job.BotTelegramBindingDailyReconciliationJob;
import com.company.vzvod.job.ServiceInfoVocationStatusDailySyncJob;
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
                        cronSchedule("0 0 0 1 1 ?")
                                .inTimeZone(TimeZone.getTimeZone("Europe/Moscow"))
                )
                .build();
    }

    @Bean
    public JobDetail serviceInfoVocationStatusDailySyncJobDetail() {
        return newJob(ServiceInfoVocationStatusDailySyncJob.class)
                .withIdentity("serviceInfoVocationStatusDailySync")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger serviceInfoVocationStatusDailySyncTrigger(JobDetail serviceInfoVocationStatusDailySyncJobDetail) {
        return newTrigger()
                .forJob(serviceInfoVocationStatusDailySyncJobDetail)
                .withIdentity("serviceInfoVocationStatusDailySyncTrigger")
                .withSchedule(
                        // Каждый день в 00:05 (по Мск), чтобы статус гарантированно "переехал" на новую дату.
                        cronSchedule("0 5 0 ? * *")
                                .inTimeZone(TimeZone.getTimeZone("Europe/Moscow"))
                )
                .build();
    }

    @Bean
    public JobDetail botTelegramBindingDailyReconciliationJobDetail() {
        return newJob(BotTelegramBindingDailyReconciliationJob.class)
                .withIdentity("botTelegramBindingDailyReconciliation")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger botTelegramBindingDailyReconciliationTrigger(
            JobDetail botTelegramBindingDailyReconciliationJobDetail
    ) {
        return newTrigger()
                .forJob(botTelegramBindingDailyReconciliationJobDetail)
                .withIdentity("botTelegramBindingDailyReconciliationTrigger")
                .withSchedule(
                        // После синхронизации статусов отпусков: снять устаревшие привязки Telegram.
                        cronSchedule("0 10 0 ? * *")
                                .inTimeZone(TimeZone.getTimeZone("Europe/Moscow"))
                )
                .build();
    }
}