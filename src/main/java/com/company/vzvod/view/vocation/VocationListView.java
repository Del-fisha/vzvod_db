package com.company.vzvod.view.vocation;

import com.company.vzvod.entity.Vocation;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "vocations", layout = MainViewTopMenu.class)
@ViewController(id = "Vocation.list")
@ViewDescriptor(path = "vocation-list-view.xml")
@LookupComponent("vocationsDataGrid")
@DialogMode(width = "64em")
public class VocationListView extends StandardListView<Vocation> {
}