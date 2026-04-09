package com.company.vzvod.view.shift;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;


@Route(value = "shifts", layout = MainView.class)
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

    @Supply(to = "shiftsDataGrid.adminViolationsCount", subject = "renderer")
    protected Renderer<Shift> adminCountRenderer() {
        return new TextRenderer<>(shift ->
                shift.getAdministrativeViolations() == null
                        ? "0"
                        : String.valueOf(shift.getAdministrativeViolations().size())
        );
    }

    @Supply(to = "shiftsDataGrid.criminalViolationsCount", subject = "renderer")
    protected Renderer<Shift> criminalCountRenderer() {
        return new TextRenderer<>(shift ->
                shift.getCriminalViolations() == null
                        ? "0"
                        : String.valueOf(shift.getCriminalViolations().size())
        );
    }

}