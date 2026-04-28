package com.company.vzvod.view.vehicle;

import com.company.vzvod.entity.User;
import com.company.vzvod.entity.Vehicle;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "vehicles/:id", layout = MainView.class)
@ViewController(id = "Vehicle.detail")
@ViewDescriptor(path = "vehicle-detail-view.xml")
@EditedEntityContainer("vehicleDc")
public class VehicleDetailView extends StandardDetailView<Vehicle> {
    @Autowired
    private CurrentAuthentication currentAuthentication;

    @Subscribe
    public void onInitEntity(final InitEntityEvent<Vehicle> event) {
        Vehicle vehicle = event.getEntity();
        if (vehicle.getUser() == null) {
            vehicle.setUser((User) currentAuthentication.getUser());
        }
    }
}