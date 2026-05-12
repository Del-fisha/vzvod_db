package com.company.vzvod.view.event;

import com.company.vzvod.entity.Event;
import com.company.vzvod.security.FullAccessRole;
import com.company.vzvod.service.event_service.EventArchiveService;
import com.company.vzvod.view.main.MainView;
import com.company.vzvod.view.deletedevent.DeletedEventListView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.UI;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.*;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

@Route(value = "events", layout = MainView.class)
@ViewController(id = "Event.list")
@ViewDescriptor(path = "event-list-view.xml")
@LookupComponent("eventsDataGrid")
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
    private Button archiveWithoutSquadButton;
    @ViewComponent
    private Button permanentDeleteButton;

    @ViewComponent
    private Button eventsPlannedBtn;
    @ViewComponent
    private Button eventsPastBtn;
    @ViewComponent
    private Button eventsWithoutSquadBtn;

    @Autowired
    private EventArchiveService eventArchiveService;

    @Subscribe
    public void onInit(InitEvent event) {
        eventsDataGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        eventsDataGrid.addSelectionListener(selection -> syncEventActionStates());

        if (eventsPastBtn != null) {
            eventsPastBtn.addClickListener(e -> UI.getCurrent().navigate(LastEventListView.class));
        }
        if (eventsWithoutSquadBtn != null) {
            eventsWithoutSquadBtn.addClickListener(e -> UI.getCurrent().navigate(DeletedEventListView.class));
        }
    }

    @Subscribe("eventsDataGrid.archiveWithoutSquadAction")
    public void onEventsDataGridArchiveWithoutSquadAction(ActionPerformedEvent apEvent) {
        Set<Event> selected = eventsDataGrid.getSelectedItems();
        if (selected.isEmpty()) {
            return;
        }

        eventArchiveService.archiveEvents(selected);
        getViewData().getLoader("eventsDl").load();
    }

    @Subscribe("eventsDataGrid.permanentDeleteAction")
    public void onEventsDataGridPermanentDeleteAction(ActionPerformedEvent apEvent) {
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

        // Переключатели списков должны быть видны всем, кто открыл view
        if (eventsPlannedBtn != null) eventsPlannedBtn.setVisible(true);
        if (eventsPastBtn != null) eventsPastBtn.setVisible(true);
        if (eventsWithoutSquadBtn != null) eventsWithoutSquadBtn.setVisible(true);

        // Перенос (БЕЗ ВЗВОДА) только при FullAccessRole
        if (archiveWithoutSquadButton != null) {
            archiveWithoutSquadButton.setVisible(hasFullAccess());
        }
        syncEventActionStates();
    }

    @Subscribe("eventsDataGrid")
    public void onEventsDataGridItemDoubleClick(ItemDoubleClickEvent<Event> apEvent) {
        if (!hasFullAccess()) {
            return;
        }
        var editAction = eventsDataGrid.getAction("editAction");
        if (editAction != null && editAction.isEnabled()) {
            editAction.actionPerform(eventsDataGrid);
        }
    }

    /**
     * list_itemTracking включает действие при выборе строки; здесь ограничиваем только финальное «Удалить»
     * ролью system-full-access (полицейский без этого — не видит кнопку).
     */
    private void syncEventActionStates() {
        boolean hasSelection = !eventsDataGrid.getSelectedItems().isEmpty();

        var archiveAct = eventsDataGrid.getAction("archiveWithoutSquadAction");
        if (archiveAct != null) {
            archiveAct.setEnabled(hasFullAccess() && hasSelection);
        }

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
