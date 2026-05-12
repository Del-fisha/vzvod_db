package com.company.vzvod.view.shift;

import com.company.vzvod.entity.Shift;
import com.company.vzvod.security.UiAccessService;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;


@Route(value = "my-shifts", layout = MainView.class)
@ViewController(id = "MyShift.list")
@ViewDescriptor(path = "my-shift-list-view.xml")
@LookupComponent("shiftsDataGrid")
@DialogMode(width = "64em")
public class MyShiftListView extends StandardListView<Shift> {

    @Autowired
    private UiAccessService uiAccessService;

    @ViewComponent
    private JmixButton removeButton;

    @ViewComponent
    private DataGrid<Shift> shiftsDataGrid;

    @Subscribe
    public void onReady(ReadyEvent event) {
        if (uiAccessService.hasFullAccessRole()) {
            return;
        }
        // Мои смены: не видит кнопку УДАЛИТЬ
        if (removeButton != null) {
            removeButton.setVisible(false);
        }
        var removeAction = shiftsDataGrid == null ? null : shiftsDataGrid.getAction("removeAction");
        if (removeAction != null) {
            removeAction.setEnabled(false);
        }
    }
}