package com.company.vzvod.view.vehicle;

import com.company.vzvod.entity.Vehicle;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "vehicles/:id", layout = MainViewTopMenu.class)
@ViewController(id = "Vehicle.detail")
@ViewDescriptor(path = "vehicle-detail-view.xml")
@EditedEntityContainer("vehicleDc")
public class VehicleDetailView extends StandardDetailView<Vehicle> {
}