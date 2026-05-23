package com.company.vzvod.service;

import com.company.vzvod.entity.Incentive;
import com.company.vzvod.entity.ServiceInfo;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class IncentiveBulkCreateService {

    private final DataManager dataManager;

    public IncentiveBulkCreateService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Transactional
    public List<Incentive> createForServiceInfos(Incentive template, Collection<ServiceInfo> serviceInfos) {
        if (template == null) {
            throw new IllegalArgumentException("template is required");
        }
        if (serviceInfos == null || serviceInfos.isEmpty()) {
            throw new IllegalArgumentException("at least one serviceInfo is required");
        }

        List<Incentive> saved = new ArrayList<>();
        for (ServiceInfo serviceInfo : serviceInfos) {
            UUID serviceInfoId = Objects.requireNonNull(serviceInfo.getId(), "serviceInfo.id");
            Incentive incentive = dataManager.create(Incentive.class);
            copyTemplateFields(template, incentive);
            incentive.setUserServiceInfo(dataManager.getReference(ServiceInfo.class, serviceInfoId));
            saved.add(dataManager.save(incentive));
        }
        return saved;
    }

    private static void copyTemplateFields(Incentive from, Incentive to) {
        to.setInitiator(from.getInitiator());
        to.setIncentiveType(from.getIncentiveType());
        to.setDate(from.getDate());
        to.setOrderNumber(from.getOrderNumber());
        to.setDescription(from.getDescription());
    }
}
