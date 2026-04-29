package com.company.vzvod.view.vehicle;

import com.company.vzvod.entity.User;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "my-vehicles", layout = MainView.class)
@ViewController(id = "MyVehicle.list")
@ViewDescriptor(path = "my-vehicle-list-view.xml")
@DialogMode(width = "64em")
public class MyVehicleListView extends VehicleListView {

    @Autowired
    private CurrentAuthentication currentAuthentication;

    @Subscribe
    public void onInit(final InitEvent event) {
        setUser((User) currentAuthentication.getUser());
    }
}

