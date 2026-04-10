package com.company.vzvod.view.event;

import com.company.vzvod.entity.Event;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.view.*;

@Route(value = "events", layout = MainView.class)
@ViewController(id = "Event.list")
@ViewDescriptor(path = "event-list-view.xml")
@LookupComponent("eventsDataGrid")
@DialogMode(width = "64em")
public class EventListView extends StandardListView<Event> {

    @ViewComponent
    private DataGrid<Event> eventsDataGrid;

    @Subscribe
    public void onInit(InitEvent event) {
        eventsDataGrid.setSelectionMode(Grid.SelectionMode.MULTI);
    }
}