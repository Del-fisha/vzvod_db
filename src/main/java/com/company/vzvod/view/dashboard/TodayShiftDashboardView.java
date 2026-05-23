package com.company.vzvod.view.dashboard;

import com.company.vzvod.service.dashboard.TodayShiftDashboardContentBuilder;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "today-shift-dashboard", layout = MainView.class)
@ViewController(id = "TodayShiftDashboardView")
@ViewDescriptor(path = "today-shift-dashboard-view.xml")
public class TodayShiftDashboardView extends StandardView {

    @ViewComponent
    private VerticalLayout rootContainer;

    @Autowired
    private TodayShiftDashboardContentBuilder dashboardContentBuilder;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        refreshDashboard();
    }

    private void refreshDashboard() {
        rootContainer.removeAll();
        rootContainer.setSpacing(true);
        rootContainer.setPadding(true);
        rootContainer.setSizeFull();
        rootContainer.addClassName("today-shift-dashboard");

        Scroller scroller = dashboardContentBuilder.buildScroller(this::refreshDashboard);
        rootContainer.add(scroller);
        rootContainer.expand(scroller);
    }
}
