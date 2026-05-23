package com.company.vzvod.view.incentive;

import com.company.vzvod.entity.Incentive;
import com.company.vzvod.entity.Initiator;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import com.company.vzvod.view.main.MainView;
import com.company.vzvod.view.shared.CurrentUserServiceInfoLoader;
import com.vaadin.flow.router.Route;
import io.jmix.core.EntityStates;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "incentives/:id", layout = MainView.class)
@ViewController(id = "Incentive.detail")
@ViewDescriptor(path = "incentive-detail-view.xml")
@EditedEntityContainer("incentiveDc")
public class IncentiveDetailView extends StandardDetailView<Incentive> {

    @Autowired
    private EntityStates entityStates;

    @Autowired
    private CurrentAuthentication currentAuthentication;

    @Autowired
    private CurrentUserServiceInfoLoader currentUserServiceInfoLoader;

    @Subscribe
    public void onInitEntity(final InitEntityEvent<Incentive> event) {
        Incentive incentive = event.getEntity();
        incentive.setInitiator(Initiator.METRO);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        Incentive incentive = getEditedEntity();
        if (incentive == null || !entityStates.isNew(incentive)) {
            return;
        }
        ensureUserServiceInfoForNewIncentive(incentive);
    }

    private void ensureUserServiceInfoForNewIncentive(Incentive incentive) {
        ServiceInfo serviceInfo = incentive.getUserServiceInfo();
        if (serviceInfo != null && serviceInfo.getUser() != null) {
            incentive.setUserServiceInfo(getViewData().getDataContext().merge(serviceInfo));
            return;
        }

        User user = (User) currentAuthentication.getUser();
        if (user != null && user.getServiceInfo() != null) {
            serviceInfo = user.getServiceInfo();
        } else {
            serviceInfo = currentUserServiceInfoLoader.loadCurrentUserServiceInfo();
        }

        if (serviceInfo != null) {
            incentive.setUserServiceInfo(getViewData().getDataContext().merge(serviceInfo));
        }
    }
}