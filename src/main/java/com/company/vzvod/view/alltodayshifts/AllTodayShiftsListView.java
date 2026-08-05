package com.company.vzvod.view.alltodayshifts;

import com.company.vzvod.entity.AllTodayShifts;
import com.company.vzvod.entity.Dep;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.entity.User;
import com.company.vzvod.util.EmployeeOrdering;
import com.company.vzvod.view.dashboard.DayShiftDashboardView;
import com.company.vzvod.view.main.MainView;
import com.company.vzvod.view.shift.ShiftDetailView;
import com.company.vzvod.view.shiftblank.ShiftBlankView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Route(value = "all-today-shifts", layout = MainView.class)
@ViewController(id = "AllTodayShifts.list")
@ViewDescriptor(path = "all-today-shifts-list-view.xml")
public class AllTodayShiftsListView extends StandardListView<AllTodayShifts> {

    private static final long ROUTE_CLICK_DELAY_MS = 280L;

    @ViewComponent
    private CollectionLoader<AllTodayShifts> daysDl;

    @ViewComponent
    private CollectionLoader<Shift> routesDl;

    @Autowired
    private ViewNavigators viewNavigators;

    private ClickVersusDoubleClickCoordinator routeClickCoordinator;
    private Shift pendingRouteShift;
    private UI pendingRouteUi;

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
    public void onRoutesDataGridItemClick(ItemClickEvent<Shift> event) {
        Shift shift = event.getItem();
        if (shift == null || shift.getId() == null) {
            return;
        }
        pendingRouteShift = shift;
        pendingRouteUi = UI.getCurrent();
        routeClickCoordinator().onClick();
    }

    @Subscribe("routesDataGrid")
    public void onRoutesDataGridItemDoubleClick(ItemDoubleClickEvent<Shift> event) {
        Shift shift = event.getItem();
        if (shift == null || shift.getId() == null) {
            return;
        }
        pendingRouteShift = shift;
        pendingRouteUi = UI.getCurrent();
        routeClickCoordinator().onDoubleClick();
    }

    @Supply(to = "routesDataGrid.employeesShortFio", subject = "renderer")
    protected Renderer<Shift> employeesShortFioRenderer() {
        return new TextRenderer<>(this::formatEmployees);
    }

    private ClickVersusDoubleClickCoordinator routeClickCoordinator() {
        if (routeClickCoordinator == null) {
            routeClickCoordinator = new ClickVersusDoubleClickCoordinator(
                    ROUTE_CLICK_DELAY_MS,
                    () -> openPendingRoute(RouteRowOpenTarget.forSingleClick()),
                    () -> openPendingRoute(RouteRowOpenTarget.forDoubleClick())
            );
        }
        return routeClickCoordinator;
    }

    private void openPendingRoute(RouteRowOpenTarget target) {
        Shift shift = pendingRouteShift;
        if (shift == null || shift.getId() == null) {
            return;
        }
        UI ui = pendingRouteUi != null ? pendingRouteUi : UI.getCurrent();
        if (ui == null) {
            openRoute(shift, target);
            return;
        }
        ui.access(() -> openRoute(shift, target));
    }

    private void openRoute(Shift shift, RouteRowOpenTarget target) {
        if (target == RouteRowOpenTarget.SHIFT_DETAIL) {
            openShiftDetail(shift);
        } else {
            openShiftBlank(shift.getId());
        }
    }

    private void loadRoutes(AllTodayShifts day) {
        LocalDate date = day.getDate();
        Dep department = day.getDepartment();
        if (date == null || department == null) {
            routesDl.setParameter("date", null);
            routesDl.setParameter("department", null);
            routesDl.load();
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

    private void openShiftBlank(UUID shiftId) {
        viewNavigators.view(this, ShiftBlankView.class)
                .withQueryParameters(QueryParameters.of("shiftId", shiftId.toString()))
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
