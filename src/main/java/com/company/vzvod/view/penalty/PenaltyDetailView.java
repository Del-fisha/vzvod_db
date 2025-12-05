package com.company.vzvod.view.penalty;

import com.company.vzvod.entity.Penalty;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "penalties/:id", layout = MainViewTopMenu.class)
@ViewController(id = "Penalty.detail")
@ViewDescriptor(path = "penalty-detail-view.xml")
@EditedEntityContainer("penaltyDc")
public class PenaltyDetailView extends StandardDetailView<Penalty> {
}