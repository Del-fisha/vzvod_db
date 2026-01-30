package com.company.vzvod.view.shift;

import com.company.vzvod.entity.Shift;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "shifts/:id", layout = MainViewTopMenu.class)
@ViewController(id = "Shift.detail")
@ViewDescriptor(path = "shift-detail-view.xml")
@EditedEntityContainer("shiftDc")
public class ShiftDetailView extends StandardDetailView<Shift> {
}