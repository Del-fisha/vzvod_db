package com.company.vzvod.view.vehicle;

import com.company.vzvod.entity.User;
import com.company.vzvod.entity.Vehicle;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;


@Route(value = "vehicles", layout = MainView.class)
@ViewController(id = "Vehicle.list")
@ViewDescriptor(path = "vehicle-list-view.xml")
@LookupComponent("vehiclesDataGrid")
@DialogMode(width = "64em")
public class VehicleListView extends StandardListView<Vehicle> {
    @ViewComponent
    private CollectionLoader<Vehicle> vehiclesDl;

    @Autowired
    private CurrentAuthentication currentAuthentication;

    private User user;

    public void setUser(User user) {
        this.user = user;
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        if (user == null) {
            user = (User) currentAuthentication.getUser();
        }
        vehiclesDl.setParameter("user", user);
        vehiclesDl.load();
    }

    @Install(to = "vehiclesDataGrid.createAction", subject = "initializer")
    private void vehiclesDataGridCreateInitializer(Vehicle vehicle) {
        if (user != null) {
            vehicle.setUser(user);
        }
    }
}