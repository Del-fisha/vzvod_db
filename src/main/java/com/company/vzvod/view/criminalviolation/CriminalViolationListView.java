package com.company.vzvod.view.criminalviolation;

import com.company.vzvod.entity.CriminalViolation;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;

import java.util.Objects;

@Route(value = "criminal-violations", layout = MainView.class)
@ViewController(id = "CriminalViolation.list")
@ViewDescriptor(path = "criminal-violation-list-view.xml")
@LookupComponent("criminalViolationsDataGrid")
@DialogMode(width = "64em")
public class CriminalViolationListView extends StandardListView<CriminalViolation> {

    @ViewComponent
    private CollectionLoader<CriminalViolation> criminalViolationsDl;

    private ServiceInfo serviceInfo;

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        criminalViolationsDl.setParameter("serviceInfo", serviceInfo);
        criminalViolationsDl.load();
    }
}