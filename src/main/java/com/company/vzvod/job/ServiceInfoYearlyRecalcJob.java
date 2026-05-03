package com.company.vzvod.job;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.service.VocationBalanceService;
import io.jmix.core.DataManager;
import io.jmix.core.security.Authenticated;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Ежегодное обновление: 01 января 00:00 (Москва, см. {@link com.company.vzvod.config.QuartzConfig}).
 * Снимает галочку медкомиссии и пересчитывает номинальный и доступный основной отпуск с учётом записей.
 */
public class ServiceInfoYearlyRecalcJob implements Job {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private VocationBalanceService vocationBalanceService;

    @Authenticated
    @Override
    public void execute(JobExecutionContext context) {
        List<ServiceInfo> all = dataManager.load(ServiceInfo.class).all().list();

        for (ServiceInfo si : all) {
            si.setMedicalExamination(false);
        }
        dataManager.save(all);

        for (ServiceInfo si : all) {
            if (si.getUser() != null && si.getStartDate() != null && si.getId() != null) {
                vocationBalanceService.recalcAndSave(si.getId());
            }
        }
    }
}
