package com.company.vzvod.listener;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import com.company.vzvod.service.VocationBalanceService;
import io.jmix.core.DataManager;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class UserArmyServiceChangedListener {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private VocationBalanceService vocationBalanceService;

    @EventListener
    public void onUserChangedBeforeCommit(EntityChangedEvent<User> event) {
        if (event.getType() == EntityChangedEvent.Type.DELETED) {
            return;
        }
        if (!event.getChanges().isChanged("armyService")) {
            return;
        }

        User user = dataManager.load(event.getEntityId()).one();
        ServiceInfo serviceInfo = user.getServiceInfo();
        if (serviceInfo == null || serviceInfo.getStartDate() == null) {
            return;
        }
        vocationBalanceService.recalcAndSave(serviceInfo.getId());
    }
}