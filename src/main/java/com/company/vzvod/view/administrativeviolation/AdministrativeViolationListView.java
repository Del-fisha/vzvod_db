package com.company.vzvod.view.administrativeviolation;

import com.company.vzvod.entity.AdministrativeViolation;
import com.company.vzvod.view.main.MainView;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "administrative-violations", layout = MainView.class)
@ViewController(id = "AdministrativeViolation.list")
@ViewDescriptor(path = "administrative-violation-list-view.xml")
@LookupComponent("administrativeViolationsDataGrid")
@DialogMode(width = "64em")
public class AdministrativeViolationListView extends StandardListView<AdministrativeViolation> {
}