package com.company.vzvod.view.vocation;

import com.company.vzvod.entity.Vocation;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "vocations/:id", layout = MainViewTopMenu.class)
@ViewController(id = "Vocation.detail")
@ViewDescriptor(path = "vocation-detail-view.xml")
@EditedEntityContainer("vocationDc")
public class VocationDetailView extends StandardDetailView<Vocation> {
}