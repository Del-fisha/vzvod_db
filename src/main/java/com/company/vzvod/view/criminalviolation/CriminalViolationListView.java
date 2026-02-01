package com.company.vzvod.view.criminalviolation;

import com.company.vzvod.entity.CriminalViolation;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "criminal-violations", layout = MainViewTopMenu.class)
@ViewController(id = "CriminalViolation.list")
@ViewDescriptor(path = "criminal-violation-list-view.xml")
@LookupComponent("criminalViolationsDataGrid")
@DialogMode(width = "64em")
public class CriminalViolationListView extends StandardListView<CriminalViolation> {
}