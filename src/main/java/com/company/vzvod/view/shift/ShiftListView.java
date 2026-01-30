package com.company.vzvod.view.shift;

import com.company.vzvod.entity.Shift;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "shifts", layout = MainViewTopMenu.class)
@ViewController(id = "Shift.list")
@ViewDescriptor(path = "shift-list-view.xml")
@LookupComponent("shiftsDataGrid")
@DialogMode(width = "64em")
public class ShiftListView extends StandardListView<Shift> {
}