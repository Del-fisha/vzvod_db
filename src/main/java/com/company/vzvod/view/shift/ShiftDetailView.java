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
import com.company.vzvod.view.serviceinfo.ServiceInfoListView;
import com.vaadin.flow.router.Route;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.list.CreateAction;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionPropertyContainer;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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

    /** После успешного сохранения не показывать диалог «есть изменения» — он оставляет пустой экран. */
    private boolean suppressUnsavedPrompt;

    @Override
    public boolean hasUnsavedChanges() {
        return !suppressUnsavedPrompt && super.hasUnsavedChanges();
    }

    @Subscribe
    public void onInitEntity(final InitEntityEvent<Shift> event) {

        Shift shift = event.getEntity();

        User user = (User) currentAuthentication.getUser();

        shift.getUnits().add(user.getServiceInfo());
        shift.setDate(LocalDate.now());
        shift.setCountOfClaims(0);
        shift.setIbdr(0);
        shift.setMigrant(0);
        shift.setCountOfStatements(0);
        shift.setDepartmentToday(DepartmentConverter.departmentFromDate(shift.getDate()));

    }

    @Subscribe(id = "shiftDc", target = Target.DATA_CONTAINER)
    public void onShiftDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<Shift> event) {
        if ("number".equals(event.getProperty())) {
            applyDefaultTypeOfShift();
        }
    }

    @Subscribe("dateField")
    public void onDateFieldTypedValueChange(
            SupportsTypedValue.TypedValueChangeEvent<TypedDatePicker<LocalDate>, LocalDate> event) {

        LocalDate date = event.getValue();
        if (date == null) {
            return;
        }
        // Не помечаем DataContext dirty, если отделение уже соответствует дате (типично при открытии).
        var next = DepartmentConverter.departmentFromDate(date);
        if (getEditedEntity().getDepartmentToday() != next) {
            getEditedEntity().setDepartmentToday(next);
        }
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        unitsDataGrid.setAllRowsVisible(true);
        fitViolationGrid(adminViolationsDataGrid);
        fitViolationGrid(criminalViolationsDataGrid);
        replaceViolationCreate(adminViolationsDataGrid, this::openNewAdministrativeViolation);
        replaceViolationCreate(criminalViolationsDataGrid, this::openNewCriminalViolation);
        configureUnitsAddAction();
        configureUnitsDetachAction();
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

    /**
     * Сортировка units / привязка полей при открытии из списка «Все смены» может
     * ложно пометить DataContext как изменённый → диалог «сохранить» и зависание оверлея.
     * После инициализации сбрасываем флаг; реальные правки пользователя снова его выставят.
     */
    @Subscribe
    public void onReady(final ReadyEvent event) {
        sortUnits();
        clearChanges();
    }

    /**
     * После сохранения DataContext иногда остаётся dirty (сортировка состава, отделение по дате).
     * Диалог «сохранить изменения» тогда открывается уже поверх закрытого экрана и оставляет
     * блокирующий оверлей: меню не нажимается, пока не обновить страницу.
     */
    @Subscribe
    public void onAfterSave(final AfterSaveEvent event) {
        suppressUnsavedPrompt = true;
        clearChanges();
    }

    @Subscribe
    public void onBeforeClose(final BeforeCloseEvent event) {
        if (event.closedWith(StandardOutcome.SAVE)) {
            suppressUnsavedPrompt = true;
            clearChanges();
        }
        releaseStuckOverlays();
    }

    @Subscribe(id = "administrativeViolationsDc", target = Target.DATA_CONTAINER)
    public void onAdministrativeViolationsDcCollectionChange(
            CollectionContainer.CollectionChangeEvent<AdministrativeViolation> event) {
        fitViolationGrid(adminViolationsDataGrid);
    }

    @Subscribe(id = "criminalViolationsDc", target = Target.DATA_CONTAINER)
    public void onCriminalViolationsDcCollectionChange(
            CollectionContainer.CollectionChangeEvent<CriminalViolation> event) {
        fitViolationGrid(criminalViolationsDataGrid);
    }

    /**
     * Высота ровно по строкам. Пустой список — одна пустая строка (min-height в теме),
     * без запаса пустых строк, который даёт высота грида по умолчанию.
     */
    private void fitViolationGrid(DataGrid<?> grid) {
        if (grid == null) {
            return;
        }
        grid.setAllRowsVisible(true);
        grid.getStyle().remove("height");
        grid.getStyle().remove("max-height");
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

    /**
     * list_remove для ManyToMany удаляет ServiceInfo из БД (Jmix RemoveOperation.saveIfNeeded).
     * Явно отвязываем сотрудника от смены, не трогая запись ServiceInfo.
     */
    private void configureUnitsAddAction() {
        Action add = unitsDataGrid.getAction("add");
        if (add instanceof BaseAction baseAdd) {
            baseAdd.withHandler(event -> openPartnerLookup());
        }
    }

    private void openPartnerLookup() {
        User user = (User) currentAuthentication.getUser();
        Set<UUID> already = new HashSet<>();
        for (ServiceInfo si : unitsDc.getItems()) {
            if (si.getId() != null) {
                already.add(si.getId());
            }
        }
        UUID shiftId = getEditedEntity().getId();
        dialogWindows.lookup(unitsDataGrid)
                .withViewClass(ServiceInfoListView.class)
                .withLookupComponentMultiSelect(true)
                .withViewConfigurer(view -> {
                    if (view instanceof ServiceInfoListView list) {
                        list.configureShiftPartnerLookup(user.getId(), shiftId, already);
                    }
                })
                .withAfterCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(StandardOutcome.SELECT)) {
                        sortUnits();
                    }
                })
                .open();
    }

    /** Модальный оверлей после диалога блокирует следующий переход и оставляет белую область. */
    private void releaseStuckOverlays() {
        getUI().ifPresent(ui -> ui.getPage().executeJs(
                """
                document.querySelectorAll('vaadin-dialog-overlay').forEach(function(ov){
                  if (!ov.opened) ov.remove();
                });
                document.body.classList.remove('vaadin-overlay-open');
                document.body.style.overflow = '';
                document.body.removeAttribute('inert');
                """));
    }

    private void configureUnitsDetachAction() {
        Action remove = unitsDataGrid.getAction("remove");
        if (remove instanceof BaseAction baseRemove) {
            baseRemove.withHandler(event -> detachSelectedUnitsFromShift());
        }
    }

    private void detachSelectedUnitsFromShift() {
        var selected = new ArrayList<>(unitsDataGrid.getSelectedItems());
        if (selected.isEmpty()) {
            return;
        }
        unitsDc.getMutableItems().removeAll(selected);
        sortUnits();
        selected.forEach(unitsDataGrid::deselect);
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
