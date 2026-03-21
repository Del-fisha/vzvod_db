package com.company.vzvod.view.vocation;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import com.company.vzvod.entity.Vocation;
import com.company.vzvod.entity.VocationType;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "vocations/:id", layout = MainView.class)
@ViewController(id = "Vocation.detail")
@ViewDescriptor(path = "vocation-detail-view.xml")
@EditedEntityContainer("vocationDc")
public class VocationDetailView extends StandardDetailView<Vocation> {


    @Autowired
    CurrentAuthentication currentAuthentication;

    @Subscribe
    public void onInitEntity(final InitEntityEvent<Vocation> event) {
        User user = (User) currentAuthentication.getUser();
        ServiceInfo serviceInfo = user.getServiceInfo();

        event.getEntity().setUserServiceInfo(serviceInfo);
        event.getEntity().setType(10);
    }
}