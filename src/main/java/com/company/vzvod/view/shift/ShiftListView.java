package com.company.vzvod.view.shift;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;


@Route(value = "shifts", layout = MainViewTopMenu.class)
@ViewController(id = "Shift.list")
@ViewDescriptor(path = "shift-list-view.xml")
@LookupComponent("shiftsDataGrid")
@DialogMode(width = "64em")
public class ShiftListView extends StandardListView<Shift> {

    @ViewComponent
    private CollectionLoader<Shift> shiftsDl;

    private ServiceInfo serviceInfo;

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        shiftsDl.setParameter("serviceInfo", serviceInfo);
        shiftsDl.load();
    }

}