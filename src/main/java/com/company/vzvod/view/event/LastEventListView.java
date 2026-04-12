package com.company.vzvod.view.event;

import com.company.vzvod.entity.Event;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "last-events-list", layout = MainView.class)
@ViewController(id = "LastEvent.list")
@ViewDescriptor(path = "last-event-list-view.xml")
@LookupComponent("eventsDataGrid")
@DialogMode(width = "64em")
public class LastEventListView extends StandardListView<Event> {
}