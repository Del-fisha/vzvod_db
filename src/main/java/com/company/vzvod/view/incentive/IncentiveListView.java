package com.company.vzvod.view.incentive;

import com.company.vzvod.entity.Incentive;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;


@Route(value = "incentives", layout = MainView.class)
@ViewController(id = "Incentive.list")
@ViewDescriptor(path = "incentive-list-view.xml")
@LookupComponent("incentivesDataGrid")
@DialogMode(width = "64em")
public class IncentiveListView extends StandardListView<Incentive> {

    @ViewComponent
    private CollectionLoader<Incentive> incentivesDl;

    private ServiceInfo serviceInfo;

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        incentivesDl.setParameter("serviceInfo", serviceInfo);
        incentivesDl.load();
    }


    @Install(to = "incentivesDataGrid.createAction", subject = "initializer")
    private void incentivesDataGridCreateInitializer(Incentive incentive) {
        // берём параметр serviceInfo, которым фильтруется список
        ServiceInfo serviceInfo = (ServiceInfo) incentivesDl.getParameter("serviceInfo");
        if (serviceInfo != null) {
            incentive.setUserServiceInfo(serviceInfo);
        }
    }
}