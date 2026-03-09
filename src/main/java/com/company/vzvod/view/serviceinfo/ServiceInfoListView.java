package com.company.vzvod.view.serviceinfo;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.view.main.MainView;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "service-infoes", layout = MainView.class)
@ViewController(id = "ServiceInfo.list")
@ViewDescriptor(path = "service-info-list-view.xml")
@LookupComponent("serviceInfoesDataGrid")
@DialogMode(width = "64em")
public class ServiceInfoListView extends StandardListView<ServiceInfo> {
}