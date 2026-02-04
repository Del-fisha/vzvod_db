package com.company.vzvod.view.penalty;

import com.company.vzvod.entity.Penalty;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;


@Route(value = "penalties", layout = MainViewTopMenu.class)
@ViewController(id = "Penalty.list")
@ViewDescriptor(path = "penalty-list-view.xml")
@LookupComponent("penaltiesDataGrid")
@DialogMode(width = "64em")
public class PenaltyListView extends StandardListView<Penalty> {

    @ViewComponent
    CollectionLoader<Penalty> penaltiesDl;

    private ServiceInfo serviceInfo;

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        penaltiesDl.setParameter("serviceInfo", serviceInfo);
        penaltiesDl.load();
    }
}