package com.company.vzvod.job;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.service.VocationService;
import io.jmix.core.DataManager;
import io.jmix.core.security.Authenticated;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

public class ServiceInfoYearlyRecalcJob implements Job {

    @Autowired
    private DataManager dataManager;

    @Authenticated
    @Override
    public void execute(JobExecutionContext context) {
        LocalDate date = LocalDate.now();

        List<ServiceInfo> all = dataManager.load(ServiceInfo.class).all().list();

        for (ServiceInfo si : all) {
            si.setMedicalExamination(false);

            if (si.getUser() != null && si.getStartDate() != null) {
                int days = VocationService.daysAvailable(si, date);
                si.setVacationDaysEntitled(days);
                si.setVacationDaysAvailable(days);
            }
        }

        dataManager.save(all);
    }
}