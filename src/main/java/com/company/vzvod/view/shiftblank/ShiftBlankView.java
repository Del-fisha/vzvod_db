package com.company.vzvod.view.shiftblank;

import com.company.vzvod.entity.AdministrativeViolation;
import com.company.vzvod.entity.CriminalViolation;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.util.EmployeeOrdering;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionPropertyContainer;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.*;

import java.util.List;
import java.util.UUID;

@Route(value = "shift-blank", layout = MainView.class)
@ViewController("ShiftBlankView")
@ViewDescriptor("shift-blank-view.xml")
public class ShiftBlankView extends StandardView {

    @ViewComponent
    private InstanceContainer<Shift> shiftDc;

    @ViewComponent
    private InstanceLoader<Shift> shiftDl;

    @ViewComponent
    private CollectionPropertyContainer<ServiceInfo> unitsDc;

    @ViewComponent
    private CollectionPropertyContainer<AdministrativeViolation> administrativeViolationsDc;

    @ViewComponent
    private CollectionPropertyContainer<CriminalViolation> criminalViolationsDc;

    @ViewComponent
    private DataGrid<ServiceInfo> unitsDataGrid;

    @ViewComponent
    private DataGrid<AdministrativeViolation> adminViolationsDataGrid;

    @ViewComponent
    private DataGrid<CriminalViolation> criminalViolationsDataGrid;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        unitsDataGrid.setAllRowsVisible(true);
        adminViolationsDataGrid.setAllRowsVisible(true);
        criminalViolationsDataGrid.setAllRowsVisible(true);
        sortUnits();
    }

    private void sortUnits() {
        if (unitsDc != null) {
            unitsDc.getMutableItems().sort(EmployeeOrdering.serviceInfoComparator());
        }
    }

    /**
     * Читаем shiftId из query-параметров и грузим смену.
     */
    @Subscribe
    public void onQueryParametersChange(QueryParametersChangeEvent event) {
        List<String> params = event.getQueryParameters()
                .getParameters()
                .get("shiftId");

        if (params == null || params.isEmpty()) {
            return;
        }

        UUID id = UUID.fromString(params.get(0));
        shiftDl.setEntityId(id);
        shiftDl.load();
        sortUnits();
    }

}