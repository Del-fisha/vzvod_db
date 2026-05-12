package com.company.vzvod.view.incentive;

import com.company.vzvod.entity.Incentive;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.security.UiAccessService;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;


@Route(value = "incentives", layout = MainView.class)
@ViewController(id = "Incentive.list")
@ViewDescriptor(path = "incentive-list-view.xml")
@LookupComponent("incentivesDataGrid")
@DialogMode(width = "64em")
public class IncentiveListView extends StandardListView<Incentive> {

    @ViewComponent
    private CollectionLoader<Incentive> incentivesDl;

    @Autowired
    private UiAccessService uiAccessService;

    @ViewComponent
    private JmixButton removeButton;

    @ViewComponent
    private io.jmix.flowui.component.grid.DataGrid<Incentive> incentivesDataGrid;

    private ServiceInfo serviceInfo;

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        incentivesDl.setParameter("serviceInfo", serviceInfo);
        incentivesDl.load();
    }

    @Subscribe
    public void onReady(ReadyEvent event) {
        if (uiAccessService.hasFullAccessRole()) {
            return;
        }
        // Поощрения: кнопка УДАЛИТЬ не активна
        if (removeButton != null) {
            removeButton.setEnabled(false);
        }
        var removeAction = incentivesDataGrid == null ? null : incentivesDataGrid.getAction("removeAction");
        if (removeAction != null) {
            removeAction.setEnabled(false);
        }
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