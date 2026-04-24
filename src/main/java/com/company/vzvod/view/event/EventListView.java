package com.company.vzvod.view.event;

import com.company.vzvod.entity.Event;
import com.company.vzvod.security.FullAccessRole;
import com.company.vzvod.service.event_service.EventArchiveService;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

@Route(value = "events", layout = MainView.class)
@ViewController(id = "Event.list")
@ViewDescriptor(path = "event-list-view.xml")
@LookupComponent("eventsDataGrid")
@DialogMode(width = "64em")
public class EventListView extends StandardListView<Event> {

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
    private Button removeButton;
    @ViewComponent
    private Button viewButton;

    @Autowired
    private EventArchiveService eventArchiveService;

    @Subscribe
    public void onInit(InitEvent event) {
        eventsDataGrid.setSelectionMode(Grid.SelectionMode.MULTI);
    }

    @Subscribe("eventsDataGrid.removeAction")
    public void onEventsDataGridRemoveAction(ActionPerformedEvent event) {
        Set<Event> selected = eventsDataGrid.getSelectedItems();
        if (selected.isEmpty()) {
            return;
        }

        eventArchiveService.archiveEvents(selected);
        getViewData().getLoader("eventsDl").load();
    }

    @Subscribe
    public void onReady(ReadyEvent event) {
        if (!hasRole(FullAccessRole.CODE)) {
            createButton.setVisible(false);
            editButton.setVisible(false);
            removeButton.setVisible(false);

            var createAction = eventsDataGrid.getAction("createAction");
            if (createAction != null) {
                createAction.setEnabled(false);
            }
            var editAction = eventsDataGrid.getAction("editAction");
            if (editAction != null) {
                editAction.setEnabled(false);
            }
            var removeAction = eventsDataGrid.getAction("removeAction");
            if (removeAction != null) {
                removeAction.setEnabled(false);
            }
        }
    }

    @Subscribe("eventsDataGrid")
    public void onEventsDataGridItemDoubleClick(ItemDoubleClickEvent<Event> event) {
        if (!hasRole(FullAccessRole.CODE)) {
            return;
        }
        var editAction = eventsDataGrid.getAction("editAction");
        if (editAction != null && editAction.isEnabled()) {
            editAction.actionPerform(eventsDataGrid);
        }
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