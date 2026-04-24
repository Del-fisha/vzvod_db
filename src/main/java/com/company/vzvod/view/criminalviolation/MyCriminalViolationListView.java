package com.company.vzvod.view.criminalviolation;

import com.company.vzvod.entity.CriminalViolation;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.view.main.MainView;
import com.company.vzvod.view.shared.CurrentUserServiceInfoLoader;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "my-criminal-violations", layout = MainView.class)
@ViewController(id = "MyCriminalViolation.list")
@ViewDescriptor(path = "my-criminal-violation-list-view.xml")
@LookupComponent("criminalViolationsDataGrid")
@DialogMode(width = "64em")
public class MyCriminalViolationListView extends StandardListView<CriminalViolation> {

    @ViewComponent
    private CollectionLoader<CriminalViolation> criminalViolationsDl;

    @Autowired
    private CurrentUserServiceInfoLoader currentUserServiceInfoLoader;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        ServiceInfo serviceInfo = currentUserServiceInfoLoader.loadCurrentUserServiceInfo();
        criminalViolationsDl.setParameter("serviceInfo", serviceInfo);
        criminalViolationsDl.load();
    }
}

