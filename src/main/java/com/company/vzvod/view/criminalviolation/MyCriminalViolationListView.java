package com.company.vzvod.view.criminalviolation;

import com.company.vzvod.entity.CriminalViolation;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.security.UiAccessService;
import com.company.vzvod.view.main.MainView;
import com.company.vzvod.view.shared.CurrentUserServiceInfoLoader;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
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

    @Autowired
    private UiAccessService uiAccessService;

    @ViewComponent
    private JmixButton removeButton;

    @ViewComponent
    private DataGrid<CriminalViolation> criminalViolationsDataGrid;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        ServiceInfo serviceInfo = currentUserServiceInfoLoader.loadCurrentUserServiceInfo();
        criminalViolationsDl.setParameter("serviceInfo", serviceInfo);
        criminalViolationsDl.load();
    }

    @Subscribe
    public void onReady(ReadyEvent event) {
        if (uiAccessService.hasFullAccessRole()) {
            return;
        }
        // Моя уголовка: не видит кнопку УДАЛИТЬ
        if (removeButton != null) {
            removeButton.setVisible(false);
        }
        var removeAction = criminalViolationsDataGrid == null ? null : criminalViolationsDataGrid.getAction("removeAction");
        if (removeAction != null) {
            removeAction.setEnabled(false);
        }
    }
}

