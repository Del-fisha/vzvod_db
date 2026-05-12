package com.company.vzvod.view.vocation;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.Vocation;
import com.company.vzvod.security.UiAccessService;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;


@Route(value = "vocations", layout = MainView.class)
@ViewController(id = "Vocation.list")
@ViewDescriptor(path = "vocation-list-view.xml")
@LookupComponent("vocationsDataGrid")
@DialogMode(width = "64em")
public class VocationListView extends StandardListView<Vocation> {

    @ViewComponent
    private CollectionLoader<Vocation> vocationsDl;

    @Autowired
    private UiAccessService uiAccessService;

    @ViewComponent
    private JmixButton removeButton;

    @ViewComponent
    private io.jmix.flowui.component.grid.DataGrid<Vocation> vocationsDataGrid;

    private ServiceInfo serviceInfo;

    public void setServiceInfo(final ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        vocationsDl.setParameter("serviceInfo", serviceInfo);
        vocationsDl.load();
    }

    @Subscribe
    public void onReady(ReadyEvent event) {
        if (uiAccessService.hasFullAccessRole()) {
            return;
        }
        // Отпуска: не видит кнопку УДАЛИТЬ
        if (removeButton != null) {
            removeButton.setVisible(false);
        }
        var removeAction = vocationsDataGrid == null ? null : vocationsDataGrid.getAction("removeAction");
        if (removeAction != null) {
            removeAction.setEnabled(false);
        }
    }

    @Install(to = "vocationsDataGrid.createAction", subject = "initializer")
    private void vocationsDataGridCreateInitializer(Vocation vocation) {
        // берём параметр serviceInfo, которым фильтруется список
        ServiceInfo serviceInfo = (ServiceInfo) vocationsDl.getParameter("serviceInfo");
        if (serviceInfo != null) {
            vocation.setUserServiceInfo(serviceInfo);
        }
    }
}