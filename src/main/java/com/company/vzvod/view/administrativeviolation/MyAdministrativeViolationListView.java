package com.company.vzvod.view.administrativeviolation;

import com.company.vzvod.entity.AdministrativeViolation;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.security.UiAccessService;
import com.company.vzvod.util.DirectoryListOrder;
import com.company.vzvod.util.SortedListLoader;
import com.company.vzvod.view.main.MainView;
import com.company.vzvod.view.shared.CurrentUserServiceInfoLoader;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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

    @Autowired
    private DataManager dataManager;

    @Autowired
    private UiAccessService uiAccessService;

    @ViewComponent
    private JmixButton removeButton;

    @ViewComponent
    private DataGrid<AdministrativeViolation> administrativeViolationsDataGrid;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        ServiceInfo serviceInfo = currentUserServiceInfoLoader.loadCurrentUserServiceInfo();
        administrativeViolationsDl.setParameter("serviceInfo", serviceInfo);
        administrativeViolationsDl.load();
    }

    @Install(to = "administrativeViolationsDl", target = Target.DATA_LOADER)
    private List<AdministrativeViolation> administrativeViolationsDlLoadDelegate(
            LoadContext<AdministrativeViolation> loadContext) {
        return SortedListLoader.load(dataManager, loadContext, DirectoryListOrder.administrativeByDate());
    }

    @Subscribe
    public void onReady(ReadyEvent event) {
        if (uiAccessService.hasFullAccessRole()) {
            return;
        }
        // Моя административка: не видит кнопку УДАЛИТЬ
        if (removeButton != null) {
            removeButton.setVisible(false);
        }
        var removeAction = administrativeViolationsDataGrid == null ? null : administrativeViolationsDataGrid.getAction("removeAction");
        if (removeAction != null) {
            removeAction.setEnabled(false);
        }
    }
}

