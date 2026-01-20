package com.company.vzvod.view.incentive;

import com.company.vzvod.entity.Incentive;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "incentives/:id", layout = MainViewTopMenu.class)
@ViewController(id = "Incentive.detail")
@ViewDescriptor(path = "incentive-detail-view.xml")
@EditedEntityContainer("incentiveDc")
public class IncentiveDetailView extends StandardDetailView<Incentive> {
}