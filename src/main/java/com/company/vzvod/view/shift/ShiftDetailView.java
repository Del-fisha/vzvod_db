package com.company.vzvod.view.shift;

import com.company.vzvod.entity.AdministrativeViolation;
import com.company.vzvod.entity.CriminalViolation;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.entity.User;
import com.company.vzvod.service.DepartmentConverter;
import com.company.vzvod.view.administrativeviolation.AdministrativeViolationDetailView;
import com.company.vzvod.view.criminalviolation.CriminalViolationDetailView;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionPropertyContainer;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

@Route(value = "shifts/:id", layout = MainView.class)
@ViewController(id = "Shift.detail")
@ViewDescriptor("shift-detail-view.xml")
@EditedEntityContainer("shiftDc")
public class ShiftDetailView extends StandardDetailView<Shift> {

    @Autowired
    private CurrentAuthentication currentAuthentication;

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

    @Subscribe
    public void onInitEntity(final InitEntityEvent<Shift> event) {

        Shift shift = event.getEntity();

        User user = (User) currentAuthentication.getUser();

        shift.getUnits().add(user.getServiceInfo());
        shift.setDate(LocalDate.now());
        shift.setCountOfClaims(0);
        shift.setIbdWithMigrant(0);
        shift.setIbdWithoutMigrant(0);
        shift.setCountOfStatements(0);
        shift.setDepartmentToday(DepartmentConverter.departmentFromDate(shift.getDate()));

    }

    @Subscribe("dateField")
    public void onDateFieldTypedValueChange(
            SupportsTypedValue.TypedValueChangeEvent<TypedDatePicker<LocalDate>, LocalDate> event) {

        LocalDate date = event.getValue();
        if (date != null) {
            getEditedEntity().setDepartmentToday(DepartmentConverter.departmentFromDate(date));
        }
    }

    @Subscribe("adminViolationsDataGrid.create")
    public void onAdminViolationsCreate(ActionPerformedEvent event) {
        DataContext dataContext = getViewData().getDataContext();
        AdministrativeViolation violation = dataContext.create(AdministrativeViolation.class);
        violation.setShift(getEditedEntity());

        dialogWindows.detail(adminViolationsDataGrid)
                .withViewClass(AdministrativeViolationDetailView.class)
                .newEntity(violation)
                .withParentDataContext(dataContext)
                .open();
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