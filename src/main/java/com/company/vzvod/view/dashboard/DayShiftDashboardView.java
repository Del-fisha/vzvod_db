package com.company.vzvod.view.dashboard;

import com.company.vzvod.entity.Dep;
import com.company.vzvod.service.dashboard.TodayShiftDashboardContentBuilder;
import com.company.vzvod.view.alltodayshifts.DayShiftDashboardParams;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

@Route(value = "day-shift-dashboard", layout = MainView.class)
@ViewController(id = "DayShiftDashboardView")
@ViewDescriptor(path = "day-shift-dashboard-view.xml")
public class DayShiftDashboardView extends StandardView {

    @ViewComponent
    private VerticalLayout rootContainer;

    @Autowired
    private TodayShiftDashboardContentBuilder dashboardContentBuilder;

    private LocalDate date;
    private Dep department;

    /**
     * Как в {@code ShiftBlankView}: Jmix ViewNavigators передаёт query через это событие,
     * а не через Vaadin {@code BeforeEnterObserver}.
     */
    @Subscribe
    public void onQueryParametersChange(QueryParametersChangeEvent event) {
        DayShiftDashboardParams.from(event.getQueryParameters().getParameters())
                .ifPresent(params -> {
                    this.date = params.date();
                    this.department = params.department();
                    refreshDashboard();
                });
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        if (date != null && department != null) {
            refreshDashboard();
        }
    }

    private void refreshDashboard() {
        rootContainer.removeAll();
        rootContainer.setSpacing(true);
        rootContainer.setPadding(true);
        rootContainer.setSizeFull();
        rootContainer.addClassName("today-shift-dashboard");

        if (date == null || department == null) {
            return;
        }

        Scroller scroller = dashboardContentBuilder.buildScroller(date, department, this::refreshDashboard);
        rootContainer.add(scroller);
        rootContainer.expand(scroller);
    }
}
