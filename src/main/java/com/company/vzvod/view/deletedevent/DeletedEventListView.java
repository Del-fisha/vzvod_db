package com.company.vzvod.view.deletedevent;

import com.company.vzvod.entity.DeletedEvent;
import com.company.vzvod.service.event_service.EventArchiveService;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.*;
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
    private Metadata metadata;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private EventArchiveService eventArchiveService;

    @Subscribe("deletedEventsDataGrid.removeAction")
    public void onDeletedEventsDataGridRemoveAction(ActionPerformedEvent event) {
        Set<DeletedEvent> selected = deletedEventsDataGrid.getSelectedItems();
        if (selected.isEmpty()) {
            return;
        }

        eventArchiveService.restoreEvents(selected);
        getViewData().getLoader("deletedEventsDl").load();
    }
}