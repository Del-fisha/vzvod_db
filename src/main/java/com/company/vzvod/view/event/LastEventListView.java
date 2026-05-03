package com.company.vzvod.view.event;

import com.company.vzvod.entity.Event;
import com.company.vzvod.security.FullAccessRole;
import com.company.vzvod.service.event_service.EventArchiveService;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.Route;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.*;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

@Route(value = "last-events-list", layout = MainView.class)
@ViewController(id = "LastEvent.list")
@ViewDescriptor(path = "last-event-list-view.xml")
@LookupComponent("eventsDataGrid")
@DialogMode(width = "64em")
public class LastEventListView extends StandardListView<Event> {

    @ViewComponent
    private DataGrid<Event> eventsDataGrid;

    @Autowired
    private CurrentAuthentication currentAuthentication;

    @Autowired
    private RoleGrantedAuthorityUtils roleGrantedAuthorityUtils;

    @ViewComponent
    private Button createButton;
    @ViewComponent
    private Button editButton;
    @ViewComponent
    private Button archiveWithoutSquadButton;
    @ViewComponent
    private Button permanentDeleteButton;

    @Autowired
    private EventArchiveService eventArchiveService;

    @Subscribe
    public void onInit(InitEvent event) {
        eventsDataGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        eventsDataGrid.addSelectionListener(selection -> syncEventActionStates());
    }

    @Subscribe("eventsDataGrid.archiveWithoutSquadAction")
    public void onArchiveWithoutSquad(ActionPerformedEvent apEvent) {
        Set<Event> selected = eventsDataGrid.getSelectedItems();
        if (selected.isEmpty()) {
            return;
        }
        eventArchiveService.archiveEvents(selected);
        getViewData().getLoader("eventsDl").load();
    }

    @Subscribe("eventsDataGrid.permanentDeleteAction")
    public void onPermanentDelete(ActionPerformedEvent apEvent) {
        Set<Event> selected = eventsDataGrid.getSelectedItems();
        if (selected.isEmpty()) {
            return;
        }
        eventArchiveService.permanentlySuppressEvents(selected);
        getViewData().getLoader("eventsDl").load();
    }

    @Subscribe
    public void onReady(ReadyEvent event) {
        if (!hasFullAccess()) {
            createButton.setVisible(false);
            editButton.setVisible(false);
            permanentDeleteButton.setVisible(false);

            var createAction = eventsDataGrid.getAction("createAction");
            if (createAction != null) {
                createAction.setEnabled(false);
            }
            var editAction = eventsDataGrid.getAction("editAction");
            if (editAction != null) {
                editAction.setEnabled(false);
            }
        } else {
            permanentDeleteButton.setVisible(true);
        }

        archiveWithoutSquadButton.setVisible(true);
        syncEventActionStates();
    }

    private void syncEventActionStates() {
        boolean hasSelection = !eventsDataGrid.getSelectedItems().isEmpty();
        var delAct = eventsDataGrid.getAction("permanentDeleteAction");
        if (delAct != null) {
            delAct.setEnabled(hasFullAccess() && hasSelection);
        }
    }

    private boolean hasFullAccess() {
        return hasRole(FullAccessRole.CODE);
    }

    private boolean hasRole(String roleCode) {
        String prefix = roleGrantedAuthorityUtils.getDefaultRolePrefix();
        return currentAuthentication.getUser().getAuthorities().stream()
                .anyMatch(grantedAuthority -> {
                    String a = grantedAuthority.getAuthority();
                    return roleCode.equals(a) || (prefix + roleCode).equals(a);
                });
    }
}
