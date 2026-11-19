package com.company.vzvod.listener;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.service.ServiceBreakRules;
import com.company.vzvod.service.VocationService;
import io.jmix.core.EntityStates;
import io.jmix.core.event.EntitySavingEvent;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Только для новой записи службы: начальные «положено»/«остаток» по номиналу стажа без учёта записей отпусков
 * (их ещё нет). Актуальный учёт по отпускам обновляется при сохранении {@link com.company.vzvod.entity.Vocation}
 * (см. {@link VocationChangedListener} и диалог отпуска).
 */
@Component
public class ServiceInfoEventListener {

    @Autowired
    private EntityStates entityStates;

    @EventListener
    public void onServiceInfoSaving(EntitySavingEvent<ServiceInfo> event) {
        ServiceInfo serviceInfo = event.getEntity();
        if (entityStates.isLoaded(serviceInfo, "serviceBreaks")) {
            String problem = ServiceBreakRules.problem(serviceInfo);
            if (problem != null) {
                throw new ValidationException(problem);
            }
        }
        if (!event.isNewEntity()) {
            return;
        }

        if (serviceInfo.getUser() == null || serviceInfo.getStartDate() == null) {
            return;
        }

        LocalDate now = LocalDate.now();
        LocalDate yearStart = LocalDate.of(now.getYear(), 1, 1);
        int nominal = VocationService.nominalDaysAvailable(serviceInfo, yearStart);
        int midYear = VocationService.midYearSeniorityBonuses(serviceInfo, yearStart, now);
        int entitled = nominal + midYear;
        serviceInfo.setVacationDaysEntitled(entitled);
        serviceInfo.setVacationDaysAvailable(entitled);
    }
}
