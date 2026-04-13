package com.company.vzvod.view.deletedevent;

import com.company.vzvod.entity.DeletedEvent;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "deleted-events", layout = MainView.class)
@ViewController(id = "DeletedEvent.list")
@ViewDescriptor(path = "deleted-event-list-view.xml")
@LookupComponent("deletedEventsDataGrid")
@DialogMode(width = "64em")
public class DeletedEventListView extends StandardListView<DeletedEvent> {
}