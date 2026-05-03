package com.company.vzvod.view.deletedevent;

import com.company.vzvod.entity.DeletedEvent;
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

@Route(value = "deleted-events", layout = MainView.class)
@ViewController(id = "DeletedEvent.list")
@ViewDescriptor(path = "deleted-event-list-view.xml")
@LookupComponent("deletedEventsDataGrid")
@DialogMode(width = "64em")
public class DeletedEventListView extends StandardListView<DeletedEvent> {

    @ViewComponent
    private DataGrid<DeletedEvent> deletedEventsDataGrid;

    @Autowired
    private EventArchiveService eventArchiveService;

    @Autowired
    private CurrentAuthentication currentAuthentication;

    @Autowired
    private RoleGrantedAuthorityUtils roleGrantedAuthorityUtils;

    @ViewComponent
    private Button permanentDeleteArchivedButton;

    @Subscribe
    public void onInit(InitEvent event) {
        deletedEventsDataGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        deletedEventsDataGrid.addSelectionListener(selection -> syncDeletedGridActions());
    }

    @Subscribe("deletedEventsDataGrid.restoreAction")
    public void onRestoreAction(ActionPerformedEvent apEvent) {
        Set<DeletedEvent> selected = deletedEventsDataGrid.getSelectedItems();
        if (selected.isEmpty()) {
            return;
        }

        eventArchiveService.restoreEvents(selected);
        getViewData().getLoader("deletedEventsDl").load();
    }

    @Subscribe("deletedEventsDataGrid.permanentDeleteArchivedAction")
    public void onPermanentDeleteArchivedAction(ActionPerformedEvent apEvent) {
        Set<DeletedEvent> selected = deletedEventsDataGrid.getSelectedItems();
        if (selected.isEmpty()) {
            return;
        }

        eventArchiveService.permanentlySuppressArchived(selected);
        getViewData().getLoader("deletedEventsDl").load();
    }

    @Subscribe
    public void onReady(ReadyEvent event) {
        // УДАЛИТЬ (навсегда) только при system-full-access; у пользователя только с PolicemanRole кнопка скрыта.
        permanentDeleteArchivedButton.setVisible(hasRole(FullAccessRole.CODE));
        syncDeletedGridActions();
    }

    private void syncDeletedGridActions() {
        boolean fullAccess = hasRole(FullAccessRole.CODE);
        boolean hasSelection = !deletedEventsDataGrid.getSelectedItems().isEmpty();

        var restore = deletedEventsDataGrid.getAction("restoreAction");
        if (restore != null) {
            restore.setEnabled(hasSelection);
        }
        var permanent = deletedEventsDataGrid.getAction("permanentDeleteArchivedAction");
        if (permanent != null) {
            permanent.setEnabled(fullAccess && hasSelection);
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
