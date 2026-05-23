package com.company.vzvod.view.incentive;

import com.company.vzvod.entity.Incentive;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.security.UiAccessService;
import com.company.vzvod.view.main.MainView;
import com.company.vzvod.view.shared.CurrentUserServiceInfoLoader;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "my-incentives", layout = MainView.class)
@ViewController(id = "MyIncentive.list")
@ViewDescriptor(path = "my-incentive-list-view.xml")
@LookupComponent("incentivesDataGrid")
@DialogMode(width = "64em")
public class MyIncentiveListView extends StandardListView<Incentive> {

    @ViewComponent
    private CollectionLoader<Incentive> incentivesDl;

    @Autowired
    private CurrentUserServiceInfoLoader currentUserServiceInfoLoader;

    @Autowired
    private UiAccessService uiAccessService;

    @ViewComponent
    private DataGrid<Incentive> incentivesDataGrid;

    @ViewComponent
    private Button createButton;

    @ViewComponent
    private Button editButton;

    @ViewComponent
    private Button removeButton;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        ServiceInfo serviceInfo = currentUserServiceInfoLoader.loadCurrentUserServiceInfo();
        incentivesDl.setParameter("serviceInfo", serviceInfo);
        incentivesDl.load();
    }

    @Subscribe
    public void onReady(ReadyEvent event) {
        if (uiAccessService.hasFullAccessRole()) {
            return;
        }
        // Мои поощрения: нет кнопок вообще, просто список, даблклик отключен
        if (createButton != null) createButton.setVisible(false);
        if (editButton != null) editButton.setVisible(false);
        if (removeButton != null) removeButton.setVisible(false);

        if (incentivesDataGrid != null) {
            var createAction = incentivesDataGrid.getAction("createAction");
            if (createAction != null) createAction.setEnabled(false);
            var editAction = incentivesDataGrid.getAction("editAction");
            if (editAction != null) editAction.setEnabled(false);
            var removeAction = incentivesDataGrid.getAction("removeAction");
            if (removeAction != null) removeAction.setEnabled(false);
        }
    }

    @Subscribe("incentivesDataGrid")
    public void onIncentivesDataGridItemDoubleClick(ItemDoubleClickEvent<Incentive> event) {
        if (!uiAccessService.hasFullAccessRole()) {
            return;
        }
    }

    @Install(to = "incentivesDataGrid.createAction", subject = "initializer")
    private void incentivesDataGridCreateInitializer(Incentive incentive) {
        ServiceInfo serviceInfo = (ServiceInfo) incentivesDl.getParameter("serviceInfo");
        if (serviceInfo == null) {
            serviceInfo = currentUserServiceInfoLoader.loadCurrentUserServiceInfo();
        }
        if (serviceInfo != null) {
            incentive.setUserServiceInfo(serviceInfo);
        }
    }
}

