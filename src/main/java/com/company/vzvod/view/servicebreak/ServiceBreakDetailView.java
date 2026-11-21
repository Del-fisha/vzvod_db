package com.company.vzvod.view.servicebreak;

import com.company.vzvod.entity.ServiceBreak;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "service-breaks/:id", layout = MainView.class)
@ViewController(id = "ServiceBreak.detail")
@ViewDescriptor(path = "service-break-detail-view.xml")
@EditedEntityContainer("serviceBreakDc")
public class ServiceBreakDetailView extends StandardDetailView<ServiceBreak> {
}
