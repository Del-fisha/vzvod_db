package com.company.vzvod.view.dashboard;

import com.company.vzvod.entity.ArticleOfAdministrative;
import com.company.vzvod.entity.TypeOfCriminal;
import com.company.vzvod.dashboard.todayshift.ShiftRouteRow;
import com.company.vzvod.dashboard.todayshift.TodayShiftDashboardSnapshot;
import com.company.vzvod.service.TodayShiftDashboardService;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Route(value = "today-shift-dashboard", layout = MainView.class)
@ViewController(id = "TodayShiftDashboardView")
@ViewDescriptor(path = "today-shift-dashboard-view.xml")
public class TodayShiftDashboardView extends StandardView {

    private static final DateTimeFormatter SHIFT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ru"));

    @ViewComponent
    private VerticalLayout rootContainer;

    @Autowired
    private TodayShiftDashboardService todayShiftDashboardService;

    @Autowired
    private MessageBundle messageBundle;

    @Autowired
    private Messages messages;

    @Autowired
    private UiComponents uiComponents;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        refreshDashboard();
    }

    private void refreshDashboard() {
        renderSnapshot(todayShiftDashboardService.loadSnapshot());
    }

    private void renderSnapshot(TodayShiftDashboardSnapshot snapshot) {
        rootContainer.removeAll();
        rootContainer.setSpacing(true);
        rootContainer.setPadding(true);
        rootContainer.setSizeFull();
        rootContainer.addClassName("today-shift-dashboard");

        Scroller scroller = uiComponents.create(Scroller.class);
        scroller.setSizeFull();
        scroller.addClassName("today-shift-dashboard__scroller");

        VerticalLayout content = uiComponents.create(VerticalLayout.class);
        content.setSpacing(true);
        content.setPadding(false);
        content.setWidthFull();
        content.addClassName("today-shift-dashboard__content");

        content.add(buildHeader(snapshot));
        content.add(buildKpiGrid(snapshot));
        content.add(buildRoutesPanel(snapshot));
        content.add(buildDetailsRow(snapshot));

        scroller.setContent(content);
        rootContainer.add(scroller);
        rootContainer.expand(scroller);
    }

    private HorizontalLayout buildHeader(TodayShiftDashboardSnapshot snapshot) {
        VerticalLayout titleBox = uiComponents.create(VerticalLayout.class);
        titleBox.setSpacing(false);
        titleBox.setPadding(false);
        titleBox.addClassName("today-shift-dashboard__title-box");

        H2 title = uiComponents.create(H2.class);
        title.setText(messageBundle.getMessage("todayShiftDashboardDialog.title"));
        title.addClassName("today-shift-dashboard__title");

        String shiftDate = snapshot.operationalDate() == null
                ? "—"
                : SHIFT_DATE_FORMAT.format(snapshot.operationalDate());
        Span subtitle = uiComponents.create(Span.class);
        subtitle.setText(MessageFormat.format(
                messageBundle.getMessage("todayShiftDashboardDialog.shiftDate"),
                shiftDate
        ));
        subtitle.addClassName("today-shift-dashboard__subtitle");

        titleBox.add(title, subtitle);

        String departmentNumber = snapshot.departmentNumber() <= 0
                ? "—"
                : String.valueOf(snapshot.departmentNumber());
        Div departmentBadge = uiComponents.create(Div.class);
        departmentBadge.addClassName("today-shift-dashboard__department-badge");
        departmentBadge.setText(MessageFormat.format(
                messageBundle.getMessage("todayShiftDashboardDialog.departmentValue"),
                departmentNumber
        ));

        JmixButton refreshBtn = uiComponents.create(JmixButton.class);
        refreshBtn.setText(messageBundle.getMessage("todayShiftDashboardDialog.refresh"));
        refreshBtn.addThemeName("primary");
        refreshBtn.addClickListener(e -> refreshDashboard());

        HorizontalLayout actions = uiComponents.create(HorizontalLayout.class);
        actions.setSpacing(true);
        actions.setAlignItems(FlexComponent.Alignment.CENTER);
        actions.add(departmentBadge, refreshBtn);

        HorizontalLayout header = uiComponents.create(HorizontalLayout.class);
        header.setWidthFull();
        header.setSpacing(true);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.addClassName("today-shift-dashboard__header");
        header.add(titleBox);
        header.add(actions);
        header.expand(titleBox);
        return header;
    }

    private FlexLayout buildKpiGrid(TodayShiftDashboardSnapshot snapshot) {
        FlexLayout grid = uiComponents.create(FlexLayout.class);
        grid.addClassName("today-shift-dashboard__kpi-grid");
        grid.setWidthFull();

        grid.add(
                kpiCard(
                        messageBundle.getMessage("todayShiftDashboardDialog.ibd"),
                        snapshot.totalIbdWithMigrant(),
                        "today-shift-dashboard__kpi-card--ibd"
                ),
                kpiCard(
                        messageBundle.getMessage("todayShiftDashboardDialog.statements"),
                        snapshot.totalStatements(),
                        "today-shift-dashboard__kpi-card--statements"
                ),
                kpiCard(
                        messageBundle.getMessage("todayShiftDashboardDialog.criminal"),
                        snapshot.totalCriminalViolations(),
                        "today-shift-dashboard__kpi-card--criminal"
                ),
                kpiCard(
                        messageBundle.getMessage("todayShiftDashboardDialog.administrative"),
                        snapshot.totalAdministrativeViolations(),
                        "today-shift-dashboard__kpi-card--administrative"
                ),
                kpiCard(
                        messageBundle.getMessage("todayShiftDashboardDialog.claims"),
                        snapshot.totalClaims(),
                        "today-shift-dashboard__kpi-card--claims"
                )
        );
        return grid;
    }

    private Div kpiCard(String label, int value, String modifierClass) {
        Div card = uiComponents.create(Div.class);
        card.addClassName("today-shift-dashboard__kpi-card");
        card.addClassName(modifierClass);

        Span valueSpan = uiComponents.create(Span.class);
        valueSpan.setText(String.valueOf(value));
        valueSpan.addClassName("today-shift-dashboard__kpi-value");

        Span labelSpan = uiComponents.create(Span.class);
        labelSpan.setText(label);
        labelSpan.addClassName("today-shift-dashboard__kpi-label");

        card.add(valueSpan, labelSpan);
        return card;
    }

    private Div buildRoutesPanel(TodayShiftDashboardSnapshot snapshot) {
        Div panel = uiComponents.create(Div.class);
        panel.addClassName("today-shift-dashboard__panel");

        H3 heading = uiComponents.create(H3.class);
        heading.setText(MessageFormat.format(
                messageBundle.getMessage("todayShiftDashboardDialog.routesCount"),
                snapshot.routes().size()
        ));
        heading.addClassName("today-shift-dashboard__panel-title");
        panel.add(heading);

        if (snapshot.routes().isEmpty()) {
            panel.add(emptyState(messageBundle.getMessage("todayShiftDashboardDialog.noRoutes")));
            return panel;
        }

        FlexLayout routesGrid = uiComponents.create(FlexLayout.class);
        routesGrid.addClassName("today-shift-dashboard__routes-grid");
        routesGrid.setWidthFull();

        for (ShiftRouteRow route : snapshot.routes()) {
            routesGrid.add(routeCard(route));
        }
        panel.add(routesGrid);
        return panel;
    }

    private Div routeCard(ShiftRouteRow route) {
        Div card = uiComponents.create(Div.class);
        card.addClassName("today-shift-dashboard__route-card");

        String routeNumber = route.routeLabel() == null || route.routeLabel().isBlank()
                ? "—"
                : route.routeLabel();
        Span routeLabel = uiComponents.create(Span.class);
        routeLabel.setText(messageBundle.getMessage("todayShiftDashboardDialog.routeNumber"));
        routeLabel.addClassName("today-shift-dashboard__route-label");

        Span routeValue = uiComponents.create(Span.class);
        routeValue.setText(routeNumber);
        routeValue.addClassName("today-shift-dashboard__route-number");

        String employees = route.employees() == null || route.employees().isBlank()
                ? messageBundle.getMessage("todayShiftDashboardDialog.noEmployees")
                : route.employees();
        Span employeesValue = uiComponents.create(Span.class);
        employeesValue.setText(employees);
        employeesValue.addClassName("today-shift-dashboard__route-employees");

        card.add(routeLabel, routeValue, employeesValue);
        return card;
    }

    private HorizontalLayout buildDetailsRow(TodayShiftDashboardSnapshot snapshot) {
        HorizontalLayout row = uiComponents.create(HorizontalLayout.class);
        row.setWidthFull();
        row.setSpacing(true);
        row.addClassName("today-shift-dashboard__details-row");
        row.add(
                breakdownPanel(
                        messageBundle.getMessage("todayShiftDashboardDialog.criminalDetails"),
                        messageBundle.getMessage("todayShiftDashboardDialog.noCriminalDetails"),
                        snapshot.criminalByType().stream()
                                .map(item -> breakdownRow(criminalTypeCaption(item.typeId()), item.count()))
                                .toList()
                ),
                breakdownPanel(
                        messageBundle.getMessage("todayShiftDashboardDialog.administrativeDetails"),
                        messageBundle.getMessage("todayShiftDashboardDialog.noAdministrativeDetails"),
                        snapshot.administrativeByArticle().stream()
                                .map(item -> breakdownRow(administrativeArticleCaption(item.articleId()), item.count()))
                                .toList()
                )
        );
        return row;
    }

    private Div breakdownPanel(String title, String emptyText, java.util.List<HorizontalLayout> rows) {
        Div panel = uiComponents.create(Div.class);
        panel.addClassName("today-shift-dashboard__panel");
        panel.addClassName("today-shift-dashboard__breakdown-panel");

        H3 heading = uiComponents.create(H3.class);
        heading.setText(title);
        heading.addClassName("today-shift-dashboard__panel-title");
        panel.add(heading);

        if (rows.isEmpty()) {
            panel.add(emptyState(emptyText));
            return panel;
        }

        VerticalLayout list = uiComponents.create(VerticalLayout.class);
        list.setSpacing(false);
        list.setPadding(false);
        list.setWidthFull();
        list.addClassName("today-shift-dashboard__breakdown-list");
        rows.forEach(list::add);
        panel.add(list);
        return panel;
    }

    private HorizontalLayout breakdownRow(String label, int count) {
        Span labelSpan = uiComponents.create(Span.class);
        labelSpan.setText(label);
        labelSpan.addClassName("today-shift-dashboard__breakdown-label");

        Span countSpan = uiComponents.create(Span.class);
        countSpan.setText(String.valueOf(count));
        countSpan.addClassName("today-shift-dashboard__breakdown-count");

        HorizontalLayout row = uiComponents.create(HorizontalLayout.class);
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.addClassName("today-shift-dashboard__breakdown-row");
        row.add(labelSpan, countSpan);
        row.expand(labelSpan);
        return row;
    }

    private Span emptyState(String text) {
        Span span = uiComponents.create(Span.class);
        span.setText(text);
        span.addClassName("today-shift-dashboard__empty");
        return span;
    }

    private String administrativeArticleCaption(int articleId) {
        ArticleOfAdministrative article = ArticleOfAdministrative.fromId(articleId);
        return article == null ? "—" : messages.getMessage(article);
    }

    private String criminalTypeCaption(int typeId) {
        TypeOfCriminal type = TypeOfCriminal.fromId(typeId);
        return type == null ? "—" : messages.getMessage(type);
    }
}
