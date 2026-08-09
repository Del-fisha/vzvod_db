package com.company.vzvod.view.alltodayshifts;

import com.company.vzvod.entity.AllTodayShifts;
import com.company.vzvod.entity.Dep;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.entity.User;
import com.company.vzvod.service.AllTodayShiftsDeleteService;
import com.company.vzvod.shift.ShiftStatusBadgeFactory;
import com.company.vzvod.util.EmployeeOrdering;
import com.company.vzvod.view.dashboard.DayShiftDashboardView;
import com.company.vzvod.view.main.MainView;
import com.company.vzvod.view.shift.ShiftDetailView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Route(value = "all-today-shifts", layout = MainView.class)
@ViewController(id = "AllTodayShifts.list")
@ViewDescriptor(path = "all-today-shifts-list-view.xml")
public class AllTodayShiftsListView extends StandardListView<AllTodayShifts> {

    @ViewComponent
    private CollectionLoader<AllTodayShifts> daysDl;

    @ViewComponent
    private CollectionLoader<Shift> routesDl;

    @ViewComponent
    private DataGrid<AllTodayShifts> daysDataGrid;

    @ViewComponent
    private DataGrid<Shift> routesDataGrid;

    @Autowired
    private ViewNavigators viewNavigators;

    @Autowired
    private AllTodayShiftsDeleteService deleteService;

    @Autowired
    private Dialogs dialogs;

    @Autowired
    private Notifications notifications;

    @Autowired
    private MessageBundle messageBundle;

    @Autowired
    private Messages messages;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        routesDl.setParameter("date", null);
        routesDl.setParameter("department", null);
        daysDl.load();
        routesDl.load();
    }

    @Subscribe("daysDataGrid")
    public void onDaysDataGridItemClick(ItemClickEvent<AllTodayShifts> event) {
        AllTodayShifts day = event.getItem();
        if (day == null) {
            return;
        }
        loadRoutes(day);
    }

    @Subscribe("daysDataGrid")
    public void onDaysDataGridItemDoubleClick(ItemDoubleClickEvent<AllTodayShifts> event) {
        AllTodayShifts day = event.getItem();
        if (day == null || day.getDate() == null || day.getDepartment() == null) {
            return;
        }
        openDayDashboard(day.getDate(), day.getDepartment());
    }

    @Subscribe("routesDataGrid")
    public void onRoutesDataGridItemDoubleClick(ItemDoubleClickEvent<Shift> event) {
        Shift shift = event.getItem();
        if (shift == null || shift.getId() == null) {
            return;
        }
        openShiftDetail(shift);
    }

    @Subscribe("removeShiftButton")
    public void onRemoveShiftButtonClick(ClickEvent<JmixButton> event) {
        Set<Shift> selected = routesDataGrid.getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            notifications.create(messageBundle.getMessage("allTodayShiftsListView.selectShiftToRemove"))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }
        List<Shift> toRemove = new ArrayList<>(selected);
        AllTodayShifts selectedDay = daysDataGrid.getSingleSelectedItem();
        LocalDate dayDate = selectedDay != null ? selectedDay.getDate() : null;
        Dep dayDepartment = selectedDay != null ? selectedDay.getDepartment() : null;

        dialogs.createOptionDialog()
                .withHeader(messageBundle.getMessage("allTodayShiftsListView.removeShift"))
                .withText(messageBundle.getMessage("allTodayShiftsListView.removeShift.confirm"))
                .withActions(
                        new DialogAction(DialogAction.Type.YES)
                                .withHandler(e -> {
                                    deleteService.deleteShifts(toRemove);
                                    daysDl.load();
                                    refreshRoutesAfterDelete(dayDate, dayDepartment);
                                }),
                        new DialogAction(DialogAction.Type.NO)
                )
                .open();
    }

    @Supply(to = "routesDataGrid.employeesShortFio", subject = "renderer")
    protected Renderer<Shift> employeesShortFioRenderer() {
        return new TextRenderer<>(this::formatEmployees);
    }

    @Supply(to = "routesDataGrid.shiftStatus", subject = "renderer")
    protected Renderer<Shift> shiftStatusRenderer() {
        return new ComponentRenderer<>(shift -> ShiftStatusBadgeFactory.create(shift, messages));
    }

    private void refreshRoutesAfterDelete(LocalDate dayDate, Dep dayDepartment) {
        if (dayDate == null || dayDepartment == null) {
            clearRoutes();
            return;
        }
        AllTodayShifts remainingDay = daysDl.getContainer().getItems().stream()
                .filter(day -> dayDate.equals(day.getDate()) && dayDepartment.equals(day.getDepartment()))
                .findFirst()
                .orElse(null);
        if (remainingDay == null) {
            clearRoutes();
            return;
        }
        daysDataGrid.select(remainingDay);
        loadRoutes(remainingDay);
    }

    private void clearRoutes() {
        routesDl.setParameter("date", null);
        routesDl.setParameter("department", null);
        routesDl.load();
    }

    private void loadRoutes(AllTodayShifts day) {
        LocalDate date = day.getDate();
        Dep department = day.getDepartment();
        if (date == null || department == null) {
            clearRoutes();
            return;
        }
        routesDl.setParameter("date", date);
        routesDl.setParameter("department", department.getId());
        routesDl.load();
    }

    private void openDayDashboard(LocalDate date, Dep department) {
        viewNavigators.view(this, DayShiftDashboardView.class)
                .withQueryParameters(new QueryParameters(Map.of(
                        "date", List.of(date.toString()),
                        "department", List.of(String.valueOf(department.getId()))
                )))
                .withBackwardNavigation(true)
                .navigate();
    }

    private void openShiftDetail(Shift shift) {
        viewNavigators.detailView(this, Shift.class)
                .withViewClass(ShiftDetailView.class)
                .editEntity(shift)
                .withBackwardNavigation(true)
                .navigate();
    }

    private String formatEmployees(Shift shift) {
        if (shift == null || shift.getUnits() == null || shift.getUnits().isEmpty()) {
            return "";
        }
        return shift.getUnits().stream()
                .sorted(EmployeeOrdering.serviceInfoComparator())
                .map(ServiceInfo::getUser)
                .filter(Objects::nonNull)
                .map(User::getShortFio)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .collect(Collectors.joining(", "));
    }
}
