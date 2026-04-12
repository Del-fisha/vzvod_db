package com.company.vzvod.view.event;

import com.company.vzvod.entity.Event;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "last-events-detail/:id", layout = MainView.class)
@ViewController(id = "LastEvent.detail")
@ViewDescriptor(path = "last-event-detail-view.xml")
@EditedEntityContainer("eventDc")
public class LastEventDetailView extends StandardDetailView<Event> {
}