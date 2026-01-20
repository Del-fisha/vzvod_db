package com.company.vzvod.view.incentive;

import com.company.vzvod.entity.Incentive;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "incentives", layout = MainViewTopMenu.class)
@ViewController(id = "Incentive.list")
@ViewDescriptor(path = "incentive-list-view.xml")
@LookupComponent("incentivesDataGrid")
@DialogMode(width = "64em")
public class IncentiveListView extends StandardListView<Incentive> {
}