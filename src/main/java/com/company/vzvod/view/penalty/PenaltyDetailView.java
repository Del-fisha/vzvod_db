package com.company.vzvod.view.penalty;

import com.company.vzvod.entity.Initiator;
import com.company.vzvod.entity.Penalty;
import com.company.vzvod.entity.PenaltyStatus;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import com.company.vzvod.view.main.MainView;
import com.company.vzvod.view.shared.CurrentUserServiceInfoLoader;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

@Route(value = "penalties/:id", layout = MainView.class)
@ViewController(id = "Penalty.detail")
@ViewDescriptor(path = "penalty-detail-view.xml")
@EditedEntityContainer("penaltyDc")
public class PenaltyDetailView extends StandardDetailView<Penalty> {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private EntityStates entityStates;

    @Autowired
    private CurrentAuthentication currentAuthentication;

    @Autowired
    private CurrentUserServiceInfoLoader currentUserServiceInfoLoader;

    @Subscribe
    public void onInitEntity(final InitEntityEvent<Penalty> event) {
        Penalty penalty = event.getEntity();
        penalty.setInitiator(Initiator.METRO);
        penalty.setPenaltyStatus(PenaltyStatus.ACTIVE);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        Penalty penalty = getEditedEntity();
        if (penalty == null || !entityStates.isNew(penalty)) {
            return;
        }
        ensureUserServiceInfoForNewPenalty(penalty);
    }

    private void ensureUserServiceInfoForNewPenalty(Penalty penalty) {
        ServiceInfo serviceInfo = penalty.getUserServiceInfo();
        if (serviceInfo != null && serviceInfo.getUser() != null) {
            penalty.setUserServiceInfo(getViewData().getDataContext().merge(serviceInfo));
            return;
        }

        User user = (User) currentAuthentication.getUser();
        if (user != null && user.getServiceInfo() != null) {
            serviceInfo = user.getServiceInfo();
        } else {
            serviceInfo = currentUserServiceInfoLoader.loadCurrentUserServiceInfo();
        }

        if (serviceInfo != null) {
            penalty.setUserServiceInfo(getViewData().getDataContext().merge(serviceInfo));
        }
    }

    @Subscribe(id = "penaltyDl", target = Target.DATA_LOADER)
    public void onPenaltyDlPostLoad(InstanceLoader.PostLoadEvent<Penalty> event) {
        Penalty penalty = event.getLoadedEntity();
        if (penalty != null && penalty.autoCompleteIfExpired(LocalDate.now())) {
            dataManager.save(penalty);
        }
    }

    @Subscribe
    public void onBeforeSave(final BeforeSaveEvent event) {
        getEditedEntity().autoCompleteIfExpired(LocalDate.now());
    }
}