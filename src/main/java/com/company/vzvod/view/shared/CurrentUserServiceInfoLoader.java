package com.company.vzvod.view.shared;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import io.jmix.core.DataManager;
import io.jmix.core.security.CurrentAuthentication;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserServiceInfoLoader {

    private final DataManager dataManager;
    private final CurrentAuthentication currentAuthentication;

    public CurrentUserServiceInfoLoader(DataManager dataManager, CurrentAuthentication currentAuthentication) {
        this.dataManager = dataManager;
        this.currentAuthentication = currentAuthentication;
    }

    public ServiceInfo loadCurrentUserServiceInfo() {
        User user = (User) currentAuthentication.getUser();
        return dataManager.load(ServiceInfo.class)
                .query("select si from ServiceInfo si where si.user = :user")
                .parameter("user", user)
                .optional()
                .orElse(null);
    }
}

