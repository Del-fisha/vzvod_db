package com.company.vzvod.view.penalty;

import com.company.vzvod.entity.Penalty;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.security.UiAccessService;
import com.company.vzvod.view.main.MainView;
import com.company.vzvod.view.shared.CurrentUserServiceInfoLoader;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.router.Route;
import com.company.vzvod.service.PenaltyExpirationService;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

@Route(value = "my-penalties", layout = MainView.class)
@ViewController(id = "MyPenalty.list")
@ViewDescriptor(path = "my-penalty-list-view.xml")
@LookupComponent("penaltiesDataGrid")
@DialogMode(width = "64em")
public class MyPenaltyListView extends StandardListView<Penalty> {

    @ViewComponent
    private CollectionLoader<Penalty> penaltiesDl;

    @Autowired
    private CurrentUserServiceInfoLoader currentUserServiceInfoLoader;

    @Autowired
    private PenaltyExpirationService penaltyExpirationService;

    @Autowired
    private UiAccessService uiAccessService;

    @ViewComponent
    private DataGrid<Penalty> penaltiesDataGrid;

    @ViewComponent
    private Button createButton;

    @ViewComponent
    private Button editButton;

    @ViewComponent
    private Button removeButton;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        ServiceInfo serviceInfo = currentUserServiceInfoLoader.loadCurrentUserServiceInfo();
        penaltiesDl.setParameter("serviceInfo", serviceInfo);
        penaltiesDl.load();
    }

    @Subscribe
    public void onReady(ReadyEvent event) {
        if (uiAccessService.hasFullAccessRole()) {
            return;
        }
        // Мои взыскания: нет кнопок вообще, просто список, даблклик отключен
        if (createButton != null) createButton.setVisible(false);
        if (editButton != null) editButton.setVisible(false);
        if (removeButton != null) removeButton.setVisible(false);

        if (penaltiesDataGrid != null) {
            var createAction = penaltiesDataGrid.getAction("createAction");
            if (createAction != null) createAction.setEnabled(false);
            var editAction = penaltiesDataGrid.getAction("editAction");
            if (editAction != null) editAction.setEnabled(false);
            var removeAction = penaltiesDataGrid.getAction("removeAction");
            if (removeAction != null) removeAction.setEnabled(false);
        }
    }

    @Subscribe("penaltiesDataGrid")
    public void onPenaltiesDataGridItemDoubleClick(ItemDoubleClickEvent<Penalty> event) {
        if (!uiAccessService.hasFullAccessRole()) {
            // отключаем dblclick-редактирование
            return;
        }
    }

    @Subscribe(id = "penaltiesDl", target = Target.DATA_LOADER)
    public void onPenaltiesDlPostLoad(CollectionLoader.PostLoadEvent<Penalty> event) {
        penaltyExpirationService.saveChanged(event.getLoadedEntities(), LocalDate.now());
    }
}

