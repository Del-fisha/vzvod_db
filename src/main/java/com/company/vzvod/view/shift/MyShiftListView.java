package com.company.vzvod.view.shift;

import com.company.vzvod.entity.Shift;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "my-shifts", layout = MainView.class)
@ViewController(id = "MyShift.list")
@ViewDescriptor(path = "my-shift-list-view.xml")
@LookupComponent("shiftsDataGrid")
@DialogMode(width = "64em")
public class MyShiftListView extends StandardListView<Shift> {
}