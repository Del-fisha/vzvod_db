package com.company.vzvod.view.shift;

import com.company.vzvod.entity.AdministrativeViolation;
import com.company.vzvod.entity.CriminalViolation;
import com.company.vzvod.entity.Impact;
import com.company.vzvod.entity.NumberOfShift;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.entity.User;
import com.company.vzvod.security.UiAccessService;
import com.company.vzvod.service.DepartmentConverter;
import com.company.vzvod.util.EmployeeOrdering;
import com.company.vzvod.view.administrativeviolation.AdministrativeViolationDetailView;
import com.company.vzvod.view.criminalviolation.CriminalViolationDetailView;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.list.CreateAction;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionPropertyContainer;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.InstanceContainer;
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

    @Autowired
    private DialogWindows dialogWindows;

    @Autowired
    private UiAccessService uiAccessService;

    @ViewComponent
    private JmixButton removeUnitBtn;
    @ViewComponent
    private JmixButton removeAdminBtn;
    @ViewComponent
    private JmixButton removeCriminalBtn;

    @ViewComponent
    private DataGrid<ServiceInfo> unitsDataGrid;

    @ViewComponent
    private DataGrid<AdministrativeViolation> adminViolationsDataGrid;

    @ViewComponent
    private DataGrid<CriminalViolation> criminalViolationsDataGrid;

    @ViewComponent
    private CollectionPropertyContainer<AdministrativeViolation> administrativeViolationsDc;

    @ViewComponent
    private CollectionPropertyContainer<CriminalViolation> criminalViolationsDc;

    @ViewComponent
    private CollectionPropertyContainer<ServiceInfo> unitsDc;

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

    @Subscribe(id = "shiftDc", target = Target.DATA_CONTAINER)
    public void onShiftDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<Shift> event) {
        if ("number".equals(event.getProperty())) {
            applyDefaultTypeOfShift();
        }
    }

    @Subscribe(id = "unitsDc", target = Target.DATA_CONTAINER)
    public void onUnitsDcCollectionChange(CollectionContainer.CollectionChangeEvent<ServiceInfo> event) {
        sortUnits();
    }

    @Subscribe("dateField")
    public void onDateFieldTypedValueChange(
            SupportsTypedValue.TypedValueChangeEvent<TypedDatePicker<LocalDate>, LocalDate> event) {

        LocalDate date = event.getValue();
        if (date != null) {
            getEditedEntity().setDepartmentToday(DepartmentConverter.departmentFromDate(date));
        }
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        unitsDataGrid.setAllRowsVisible(true);
        adminViolationsDataGrid.setAllRowsVisible(true);
        criminalViolationsDataGrid.setAllRowsVisible(true);
        replaceViolationCreate(adminViolationsDataGrid, this::openNewAdministrativeViolation);
        replaceViolationCreate(criminalViolationsDataGrid, this::openNewCriminalViolation);
        sortUnits();

        if (!uiAccessService.hasFullAccessRole()) {
            // detailShift: нет кнопки УДАЛИТЬ у списка сотрудников, списка админок, списка уголовок.
            if (removeUnitBtn != null) removeUnitBtn.setVisible(false);
            if (removeAdminBtn != null) removeAdminBtn.setVisible(false);
            if (removeCriminalBtn != null) removeCriminalBtn.setVisible(false);

            var unitsRemove = unitsDataGrid.getAction("remove");
            if (unitsRemove != null) unitsRemove.setEnabled(false);
            var adminRemove = adminViolationsDataGrid.getAction("remove");
            if (adminRemove != null) adminRemove.setEnabled(false);
            var criminalRemove = criminalViolationsDataGrid.getAction("remove");
            if (criminalRemove != null) criminalRemove.setEnabled(false);
        }
    }

    private void applyDefaultTypeOfShift() {
        NumberOfShift route = getEditedEntity().getNumber();
        if (route != null) {
            getEditedEntity().setTypeOfShift(route.defaultTypeOfShift());
        }
    }

    private void sortUnits() {
        if (unitsDc == null) {
            return;
        }
        unitsDc.getMutableItems().sort(EmployeeOrdering.serviceInfoComparator());
    }

    private void replaceViolationCreate(DataGrid<?> grid, Runnable openNew) {
        Action action = grid.getAction("create");
        if (!(action instanceof CreateAction<?> createAction)) {
            return;
        }
        createAction.withHandler(null);
        createAction.withHandler(e -> openNew.run());
    }

    private void openNewAdministrativeViolation() {
        DataContext parentDc = getViewData().getDataContext();
        AdministrativeViolation v = parentDc.create(AdministrativeViolation.class);
        v.setShift(getEditedEntity());
        v.setImpact(Impact.WITHOUT_IMPACT);
        dialogWindows.detail(this, AdministrativeViolation.class)
                .withListDataComponent(adminViolationsDataGrid)
                .withParentDataContext(parentDc)
                .withViewClass(AdministrativeViolationDetailView.class)
                .newEntity(v)
                .open();
    }

    private void openNewCriminalViolation() {
        DataContext parentDc = getViewData().getDataContext();
        CriminalViolation v = parentDc.create(CriminalViolation.class);
        v.setShift(getEditedEntity());
        v.setImpact(Impact.WITHOUT_IMPACT);
        dialogWindows.detail(this, CriminalViolation.class)
                .withListDataComponent(criminalViolationsDataGrid)
                .withParentDataContext(parentDc)
                .withViewClass(CriminalViolationDetailView.class)
                .newEntity(v)
                .open();
    }

}
