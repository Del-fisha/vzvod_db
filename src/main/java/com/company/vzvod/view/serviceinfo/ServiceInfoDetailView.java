package com.company.vzvod.view.serviceinfo;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "service-infoes/:id", layout = MainViewTopMenu.class)
@ViewController(id = "ServiceInfo.detail")
@ViewDescriptor(path = "service-info-detail-view.xml")
@EditedEntityContainer("serviceInfoDc")
public class ServiceInfoDetailView extends StandardDetailView<ServiceInfo> {
}