package com.company.vzvod.view.penalty;

import com.company.vzvod.entity.Penalty;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "penalties", layout = MainViewTopMenu.class)
@ViewController(id = "Penalty.list")
@ViewDescriptor(path = "penalty-list-view.xml")
@LookupComponent("penaltiesDataGrid")
@DialogMode(width = "64em")
public class PenaltyListView extends StandardListView<Penalty> {
}