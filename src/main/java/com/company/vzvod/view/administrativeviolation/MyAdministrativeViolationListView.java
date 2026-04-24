package com.company.vzvod.view.administrativeviolation;

import com.company.vzvod.entity.AdministrativeViolation;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.view.main.MainView;
import com.company.vzvod.view.shared.CurrentUserServiceInfoLoader;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "my-administrative-violations", layout = MainView.class)
@ViewController(id = "MyAdministrativeViolation.list")
@ViewDescriptor(path = "my-administrative-violation-list-view.xml")
@LookupComponent("administrativeViolationsDataGrid")
@DialogMode(width = "64em")
public class MyAdministrativeViolationListView extends StandardListView<AdministrativeViolation> {

    @ViewComponent
    private CollectionLoader<AdministrativeViolation> administrativeViolationsDl;

    @Autowired
    private CurrentUserServiceInfoLoader currentUserServiceInfoLoader;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        ServiceInfo serviceInfo = currentUserServiceInfoLoader.loadCurrentUserServiceInfo();
        administrativeViolationsDl.setParameter("serviceInfo", serviceInfo);
        administrativeViolationsDl.load();
    }
}

