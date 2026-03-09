package com.company.vzvod.view.incentive;

import com.company.vzvod.entity.Incentive;
import com.company.vzvod.entity.Initiator;
import com.company.vzvod.view.main.MainView;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "incentives/:id", layout = MainView.class)
@ViewController(id = "Incentive.detail")
@ViewDescriptor(path = "incentive-detail-view.xml")
@EditedEntityContainer("incentiveDc")
public class IncentiveDetailView extends StandardDetailView<Incentive> {

    @Subscribe
    public void onInitEntity(final InitEntityEvent<Incentive> event) {

        Incentive incentive = event.getEntity();

        incentive.setInitiator(Initiator.METRO);
    }

}