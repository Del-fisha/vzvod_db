package com.company.vzvod.view.penalty;

import com.company.vzvod.entity.Initiator;
import com.company.vzvod.entity.Penalty;
import com.company.vzvod.entity.PenaltyStatus;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "penalties/:id", layout = MainView.class)
@ViewController(id = "Penalty.detail")
@ViewDescriptor(path = "penalty-detail-view.xml")
@EditedEntityContainer("penaltyDc")
public class PenaltyDetailView extends StandardDetailView<Penalty> {

    @Subscribe
    public void onInitEntity(final InitEntityEvent<Penalty> event) {

        Penalty penalty = event.getEntity();

        penalty.setInitiator(Initiator.METRO);
        penalty.setPenaltyStatus(PenaltyStatus.ACTIVE);
    }
}