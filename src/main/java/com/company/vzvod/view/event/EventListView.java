package com.company.vzvod.view.event;

import com.company.vzvod.entity.DeletedEvent;
import com.company.vzvod.entity.Event;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
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
    private Metadata metadata;

    @Autowired
    private DataManager dataManager;

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
        for (Event e : selected) {
            DeletedEvent de = metadata.create(DeletedEvent.class);

            de.setOriginalEventId(e.getId());
            de.setEventType(e.getEventType());
            de.setPlace(e.getPlace());
            de.setName(e.getName());
            de.setDate(e.getDate());
            de.setTime(e.getTime());
            de.setShiftOfDepartment(e.getShiftOfDepartment());
            de.setDescription(e.getDescription());

            dataManager.save(de);
            dataManager.remove(e);
        }
        getViewData().getLoader("eventsDl").load();
    }
}