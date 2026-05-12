package com.company.vzvod.view.penalty;

import com.company.vzvod.entity.Penalty;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.security.UiAccessService;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Route(value = "penalties", layout = MainView.class)
@ViewController(id = "Penalty.list")
@ViewDescriptor(path = "penalty-list-view.xml")
@LookupComponent("penaltiesDataGrid")
@DialogMode(width = "64em")
public class PenaltyListView extends StandardListView<Penalty> {

    @ViewComponent
    CollectionLoader<Penalty> penaltiesDl;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private UiAccessService uiAccessService;

    @ViewComponent
    private JmixButton removeButton;

    @ViewComponent
    private io.jmix.flowui.component.grid.DataGrid<Penalty> penaltiesDataGrid;

    private ServiceInfo serviceInfo;

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        penaltiesDl.setParameter("serviceInfo", serviceInfo);
        penaltiesDl.load();
    }

    @Subscribe(id = "penaltiesDl", target = Target.DATA_LOADER)
    public void onPenaltiesDlPostLoad(CollectionLoader.PostLoadEvent<Penalty> event) {
        List<Penalty> toSave = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (Penalty p : event.getLoadedEntities()) {
            if (p != null && p.autoCompleteIfExpired(now)) {
                toSave.add(p);
            }
        }

        if (!toSave.isEmpty()) {
            dataManager.save(new SaveContext().saving(toSave));
        }
    }

    @Subscribe
    public void onReady(ReadyEvent event) {
        if (uiAccessService.hasFullAccessRole()) {
            return;
        }
        // Взыскания: кнопка УДАЛИТЬ не активна
        if (removeButton != null) {
            removeButton.setEnabled(false);
        }
        var removeAction = penaltiesDataGrid == null ? null : penaltiesDataGrid.getAction("removeAction");
        if (removeAction != null) {
            removeAction.setEnabled(false);
        }
    }

    @Install(to = "penaltiesDataGrid.createAction", subject = "initializer")
    private void penaltiesDataGridCreateInitializer(Penalty penalty) {
        // берём параметр serviceInfo, которым фильтруется список
        ServiceInfo serviceInfo = (ServiceInfo) penaltiesDl.getParameter("serviceInfo");
        if (serviceInfo != null) {
            penalty.setUserServiceInfo(serviceInfo);
        }
    }
}