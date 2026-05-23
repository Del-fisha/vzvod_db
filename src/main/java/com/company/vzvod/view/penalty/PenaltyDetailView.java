package com.company.vzvod.view.penalty;

import com.company.vzvod.entity.Initiator;
import com.company.vzvod.entity.Penalty;
import com.company.vzvod.entity.PenaltyStatus;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
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

    @Subscribe
    public void onInitEntity(final InitEntityEvent<Penalty> event) {
        Penalty penalty = event.getEntity();
        penalty.setInitiator(Initiator.METRO);
        penalty.setPenaltyStatus(PenaltyStatus.ACTIVE);
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