package com.company.vzvod.view.deletedevent;

import com.company.vzvod.entity.DeletedEvent;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "deleted-events/:id", layout = MainView.class)
@ViewController(id = "DeletedEvent.detail")
@ViewDescriptor(path = "deleted-event-detail-view.xml")
@EditedEntityContainer("deletedEventDc")
public class DeletedEventDetailView extends StandardDetailView<DeletedEvent> {
}