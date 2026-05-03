package com.company.vzvod.view.vehicle;

import com.company.vzvod.entity.User;
import com.company.vzvod.entity.Vehicle;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "my-vehicles", layout = MainView.class)
@ViewController(id = "MyVehicle.list")
@ViewDescriptor(path = "my-vehicle-list-view.xml")
@LookupComponent("vehiclesDataGrid")
@DialogMode(width = "64em")
public class MyVehicleListView extends StandardListView<Vehicle> {

    @ViewComponent
    private CollectionLoader<Vehicle> vehiclesDl;

    @Autowired
    private CurrentAuthentication currentAuthentication;

    @Autowired
    private Messages messages;

    @Override
    public String getPageTitle() {
        return messages.getMessage("com.company.vzvod.view.vehicle", "myVehicleListView.title");
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        vehiclesDl.setParameter("user", (User) currentAuthentication.getUser());
        vehiclesDl.load();
    }

    @Install(to = "vehiclesDataGrid.createAction", subject = "initializer")
    private void vehiclesDataGridCreateInitializer(Vehicle vehicle) {
        vehicle.setUser((User) currentAuthentication.getUser());
    }
}
