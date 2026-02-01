package com.company.vzvod.view.shift;

import com.company.vzvod.entity.AdministrativeViolation;
import com.company.vzvod.entity.CriminalViolation;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.view.administrativeviolation.AdministrativeViolationDetailView;
import com.company.vzvod.view.criminalviolation.CriminalViolationDetailView;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionPropertyContainer;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "shifts/:id", layout = MainView.class)
@ViewController(id = "Shift.detail")
@ViewDescriptor("shift-detail-view.xml")
@EditedEntityContainer("shiftDc")
public class ShiftDetailView extends StandardDetailView<Shift> {

    @ViewComponent
    private DataGrid<AdministrativeViolation> adminViolationsDataGrid;

    @ViewComponent
    private DataGrid<CriminalViolation> criminalViolationsDataGrid;

    @ViewComponent
    private CollectionPropertyContainer<AdministrativeViolation> administrativeViolationsDc;

    @ViewComponent
    private CollectionPropertyContainer<CriminalViolation> criminalViolationsDc;

    @ViewComponent
    private DataContext dataContext;

    @Autowired
    private DialogWindows dialogWindows;

    // КНОПКА "Добавить/Редактировать АП"
    @Subscribe("adminViolationsDataGrid.create")
    public void onAdminViolationsCreate(ActionPerformedEvent event) {
        DataContext dataContext = getViewData().getDataContext();
        AdministrativeViolation violation = dataContext.create(AdministrativeViolation.class);
        violation.setShift(getEditedEntity());  // Только set shift, НЕ добавляем в список!

        dialogWindows.detail(adminViolationsDataGrid)
                .withViewClass(AdministrativeViolationDetailView.class)
                .newEntity(violation)
                .withParentDataContext(dataContext)
                .open();  // ← Добавил ParentDataContext — violation сохранится в контексте Shift
    }

    @Subscribe("criminalViolationsDataGrid.create")
    public void onCriminalViolationsCreate(ActionPerformedEvent event) {
        DataContext dataContext = getViewData().getDataContext();
        CriminalViolation violation = dataContext.create(CriminalViolation.class);
        violation.setShift(getEditedEntity());  // Только set shift!

        dialogWindows.detail(criminalViolationsDataGrid)
                .withViewClass(CriminalViolationDetailView.class)
                .newEntity(violation)
                .withParentDataContext(dataContext)
                .open();
    }

}