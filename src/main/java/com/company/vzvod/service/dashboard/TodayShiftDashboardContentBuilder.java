package com.company.vzvod.service.dashboard;

import com.company.vzvod.dashboard.todayshift.PeriodMetricRow;
import com.company.vzvod.dashboard.todayshift.RouteCheckChipParts;
import com.company.vzvod.dashboard.todayshift.RouteChecksRow;
import com.company.vzvod.dashboard.todayshift.RouteDetailsRow;
import com.company.vzvod.dashboard.todayshift.ShiftRouteRow;
import com.company.vzvod.dashboard.todayshift.TodayShiftDashboardSnapshot;
import com.company.vzvod.entity.ArticleOfAdministrative;
import com.company.vzvod.entity.Dep;
import com.company.vzvod.entity.TypeOfCriminal;
import com.company.vzvod.service.RouteCheckService;
import com.company.vzvod.service.TodayShiftDashboardService;
import com.company.vzvod.shift.ShiftStatusBadgeFactory;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.timepicker.TimePicker;
import io.jmix.core.Messages;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.kit.component.button.JmixButton;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Сборка UI дашборда вне hot-deploy пакета view, чтобы DTO и сервис
 * загружались одним class loader (избегаем LinkageError в dev-режиме Jmix).
 */
@Component
public class TodayShiftDashboardContentBuilder {

    private static final String MSG_PREFIX = "com.company.vzvod.view.dashboard/todayShiftDashboardDialog.";
    private static final DateTimeFormatter SHIFT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ru"));

    private final TodayShiftDashboardService todayShiftDashboardService;
    private final RouteCheckService routeCheckService;
    private final Messages messages;
    private final UiComponents uiComponents;

    public TodayShiftDashboardContentBuilder(
            TodayShiftDashboardService todayShiftDashboardService,
            RouteCheckService routeCheckService,
            Messages messages,
            UiComponents uiComponents
    ) {
        this.todayShiftDashboardService = todayShiftDashboardService;
        this.routeCheckService = routeCheckService;
        this.messages = messages;
        this.uiComponents = uiComponents;
    }

    public Scroller buildScroller(Runnable onRefresh) {
        return buildScroller(todayShiftDashboardService.loadSnapshot(), onRefresh);
    }

    public Scroller buildScroller(LocalDate date, Dep department, Runnable onRefresh) {
        return buildScroller(todayShiftDashboardService.loadSnapshot(date, department), onRefresh);
    }

    public Scroller buildScroller(TodayShiftDashboardSnapshot snapshot, Runnable onRefresh) {
        Scroller scroller = uiComponents.create(Scroller.class);
        scroller.setSizeFull();
        scroller.addClassName("today-shift-dashboard__scroller");

        VerticalLayout content = uiComponents.create(VerticalLayout.class);
        content.setSpacing(true);
        content.setPadding(false);
        content.setWidthFull();
        content.addClassName("today-shift-dashboard__content");

        content.add(buildHeader(snapshot, onRefresh));
        content.add(buildRouteChecksPanel(snapshot, onRefresh));
        content.add(buildKpiGrid(snapshot));
        content.add(buildRoutesPanel(snapshot));
        content.add(buildDetailsRow(snapshot));
        content.add(buildPeriodPanel(snapshot));

        scroller.setContent(content);
        return scroller;
    }

    private String msg(String key) {
        return messages.getMessage(MSG_PREFIX + key);
    }

    private HorizontalLayout buildHeader(TodayShiftDashboardSnapshot snapshot, Runnable onRefresh) {
        VerticalLayout titleBox = uiComponents.create(VerticalLayout.class);
        titleBox.setSpacing(false);
        titleBox.setPadding(false);
        titleBox.addClassName("today-shift-dashboard__title-box");

        H2 title = uiComponents.create(H2.class);
        title.setText(msg("title"));
        title.addClassName("today-shift-dashboard__title");

        String shiftDate = snapshot.operationalDate() == null
                ? "—"
                : SHIFT_DATE_FORMAT.format(snapshot.operationalDate());
        Span subtitle = uiComponents.create(Span.class);
        subtitle.setText(MessageFormat.format(
                msg("shiftDate"),
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
                msg("departmentValue"),
                departmentNumber
        ));

        JmixButton refreshBtn = uiComponents.create(JmixButton.class);
        refreshBtn.setText(msg("refresh"));
        refreshBtn.addThemeName("primary");
        refreshBtn.addClickListener(e -> onRefresh.run());

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

    private Div buildRouteChecksPanel(TodayShiftDashboardSnapshot snapshot, Runnable onRefresh) {
        Div panel = uiComponents.create(Div.class);
        panel.addClassName("today-shift-dashboard__panel");
        panel.addClassName("today-shift-dashboard__route-checks-panel");

        H3 heading = uiComponents.create(H3.class);
        heading.setText(msg("routeChecks"));
        heading.addClassName("today-shift-dashboard__panel-title");
        panel.add(heading);

        List<RouteChecksRow> rows = snapshot.routeChecks() == null ? List.of() : snapshot.routeChecks();
        if (rows.isEmpty()) {
            panel.add(emptyState(msg("noRouteChecks")));
            return panel;
        }

        Div table = uiComponents.create(Div.class);
        table.addClassName("today-shift-dashboard__route-checks-table");

        for (RouteChecksRow row : rows) {
            Div line = uiComponents.create(Div.class);
            line.addClassName("today-shift-dashboard__route-checks-row");

            Span route = uiComponents.create(Span.class);
            route.setText(row.routeLabel() == null || row.routeLabel().isBlank() ? "—" : row.routeLabel());
            route.addClassName("today-shift-dashboard__route-checks-route");

            Div checks = uiComponents.create(Div.class);
            checks.addClassName("today-shift-dashboard__route-checks-times");
            if (row.checks() == null || row.checks().isEmpty()) {
                Span empty = uiComponents.create(Span.class);
                empty.setText("—");
                empty.addClassName("today-shift-dashboard__route-checks-empty");
                checks.add(empty);
            } else {
                boolean first = true;
                for (RouteChecksRow.RouteCheckEntry entry : row.checks()) {
                    if (!first) {
                        Span sep = uiComponents.create(Span.class);
                        sep.setText(" — ");
                        sep.addClassName("today-shift-dashboard__route-checks-sep");
                        checks.add(sep);
                    }
                    first = false;
                    checks.add(buildRouteCheckChip(entry, onRefresh));
                }
            }
            line.add(route, checks);
            table.add(line);
        }
        panel.add(table);
        return panel;
    }

    private Div buildRouteCheckChip(RouteChecksRow.RouteCheckEntry entry, Runnable onRefresh) {
        RouteCheckChipParts parts = RouteCheckChipParts.of(entry.checkedAt(), entry.checkerLabel());

        // Div (block), не Span (inline): ФИО гарантированно под временем без зависимости от CSS темы.
        Div chip = uiComponents.create(Div.class);
        chip.addClassName("today-shift-dashboard__route-checks-chip");
        chip.getStyle()
                .set("display", "inline-flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("line-height", "1.2")
                .set("text-align", "center")
                .set("cursor", "pointer");
        chip.getElement().setAttribute("title", msg("routeCheckEditHint"));
        chip.addClickListener(e -> openRouteCheckDialog(entry, onRefresh));

        Div time = uiComponents.create(Div.class);
        time.setText(parts.time());
        time.addClassName("today-shift-dashboard__route-checks-chip-time");
        time.getStyle()
                .set("display", "block")
                .set("font-weight", "600")
                .set("text-decoration", "underline dotted")
                .set("text-underline-offset", "2px");
        chip.add(time);

        if (parts.name() != null && !parts.name().isBlank()) {
            Div name = uiComponents.create(Div.class);
            name.setText(parts.name());
            name.addClassName("today-shift-dashboard__route-checks-chip-name");
            name.getStyle()
                    .set("display", "block")
                    .set("font-size", "var(--lumo-font-size-xs)")
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("white-space", "nowrap");
            chip.add(name);
        }
        return chip;
    }

    private void openRouteCheckDialog(RouteChecksRow.RouteCheckEntry entry, Runnable onRefresh) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(msg("routeCheckEditTitle"));
        dialog.setWidth("360px");
        dialog.setMaxHeight("none");
        dialog.addClassName("today-shift-dashboard__route-checks-dialog");

        TimePicker timePicker = new TimePicker();
        timePicker.setLabel(msg("routeCheckTime"));
        timePicker.setStep(java.time.Duration.ofMinutes(1));
        timePicker.setValue(entry.checkedAt());
        timePicker.setWidthFull();
        timePicker.setAutoOpen(false);

        String checkerName = entry.checkerLabel() == null || entry.checkerLabel().isBlank()
                ? "—"
                : entry.checkerLabel().trim();
        Span checker = new Span(checkerName);
        checker.addClassName("today-shift-dashboard__route-checks-dialog-checker");

        JmixButton save = uiComponents.create(JmixButton.class);
        save.setText(msg("routeCheckSave"));
        save.addThemeName("primary");
        save.addClickListener(e -> {
            LocalTime value = timePicker.getValue();
            if (value == null) {
                return;
            }
            routeCheckService.updateCheckFromUi(entry.id(), value);
            dialog.close();
            onRefresh.run();
        });

        JmixButton delete = uiComponents.create(JmixButton.class);
        delete.setText(msg("routeCheckDelete"));
        delete.addThemeName("error");
        delete.addClickListener(e -> {
            routeCheckService.deleteCheckFromUi(entry.id());
            dialog.close();
            onRefresh.run();
        });

        JmixButton cancel = uiComponents.create(JmixButton.class);
        cancel.setText(msg("routeCheckCancel"));
        cancel.addClickListener(e -> dialog.close());

        HorizontalLayout actions = uiComponents.create(HorizontalLayout.class);
        actions.setSpacing(true);
        actions.setPadding(false);
        actions.setWidthFull();
        actions.addClassName("today-shift-dashboard__route-checks-dialog-actions");
        actions.add(save, delete, cancel);

        VerticalLayout body = uiComponents.create(VerticalLayout.class);
        body.setPadding(false);
        body.setSpacing(false);
        body.setWidthFull();
        body.addClassName("today-shift-dashboard__route-checks-dialog-body");
        body.add(timePicker, checker, actions);
        dialog.add(body);
        dialog.open();
    }

    private FlexLayout buildKpiGrid(TodayShiftDashboardSnapshot snapshot) {
        FlexLayout grid = uiComponents.create(FlexLayout.class);
        grid.addClassName("today-shift-dashboard__kpi-grid");
        grid.setWidthFull();

        grid.add(
                kpiCard(
                        msg("ibd"),
                        snapshot.totalIbdr(),
                        "today-shift-dashboard__kpi-card--ibd"
                ),
                kpiCard(
                        msg("migrant"),
                        snapshot.totalMigrant(),
                        "today-shift-dashboard__kpi-card--migrant"
                ),
                kpiCard(
                        msg("statements"),
                        snapshot.totalStatements(),
                        "today-shift-dashboard__kpi-card--statements"
                ),
                kpiCard(
                        msg("criminal"),
                        snapshot.totalCriminalViolations(),
                        "today-shift-dashboard__kpi-card--criminal"
                ),
                kpiCard(
                        msg("administrative"),
                        snapshot.totalAdministrativeViolations(),
                        "today-shift-dashboard__kpi-card--administrative"
                ),
                kpiCard(
                        msg("claims"),
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
                msg("routesCount"),
                snapshot.routes().size()
        ));
        heading.addClassName("today-shift-dashboard__panel-title");
        panel.add(heading);

        if (snapshot.routes().isEmpty()) {
            panel.add(emptyState(msg("noRoutes")));
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
        routeLabel.setText(msg("routeNumber"));
        routeLabel.addClassName("today-shift-dashboard__route-label");

        Span routeValue = uiComponents.create(Span.class);
        routeValue.setText(routeNumber);
        routeValue.addClassName("today-shift-dashboard__route-number");

        String employees = route.employees() == null || route.employees().isBlank()
                ? msg("noEmployees")
                : route.employees();
        Span employeesValue = uiComponents.create(Span.class);
        employeesValue.setText(employees);
        employeesValue.addClassName("today-shift-dashboard__route-employees");

        HorizontalLayout status = ShiftStatusBadgeFactory.create(route.endTime(), messages);
        status.addClassName("today-shift-dashboard__route-status");

        card.add(routeLabel, routeValue, status, employeesValue);
        return card;
    }

    private HorizontalLayout buildDetailsRow(TodayShiftDashboardSnapshot snapshot) {
        HorizontalLayout row = uiComponents.create(HorizontalLayout.class);
        row.setWidthFull();
        row.setSpacing(true);
        row.addClassName("today-shift-dashboard__details-row");
        row.add(
                narrowBreakdownPanel(
                        msg("criminalDetails"),
                        msg("noCriminalDetails"),
                        snapshot.criminalByType().stream()
                                .map(item -> breakdownRow(criminalTypeCaption(item.typeId()), item.count()))
                                .toList()
                ),
                narrowBreakdownPanel(
                        msg("administrativeDetails"),
                        msg("noAdministrativeDetails"),
                        snapshot.administrativeByArticle().stream()
                                .map(item -> breakdownRow(administrativeArticleCaption(item.articleId()), item.count()))
                                .toList()
                ),
                buildRouteDetailsPanel(snapshot.routeDetails())
        );
        return row;
    }

    private Div buildRouteDetailsPanel(List<RouteDetailsRow> routeDetails) {
        Div panel = uiComponents.create(Div.class);
        panel.addClassName("today-shift-dashboard__panel");
        panel.addClassName("today-shift-dashboard__route-details-panel");

        H3 heading = uiComponents.create(H3.class);
        heading.setText(msg("routeDetails"));
        heading.addClassName("today-shift-dashboard__panel-title");
        panel.add(heading);

        if (routeDetails.isEmpty()) {
            panel.add(emptyState(msg("noRouteDetails")));
            return panel;
        }

        Div matrixWrap = uiComponents.create(Div.class);
        matrixWrap.addClassName("today-shift-dashboard__route-details-matrix-wrap");
        matrixWrap.add(buildRouteDetailsMatrix(routeDetails));
        panel.add(matrixWrap);
        return panel;
    }

    private Div buildRouteDetailsMatrix(List<RouteDetailsRow> rows) {
        Div matrix = uiComponents.create(Div.class);
        matrix.addClassName("today-shift-dashboard__matrix");
        matrix.addClassName("today-shift-dashboard__matrix--routes");

        Div sectionRow = matrixRow("today-shift-dashboard__matrix-row--sections");
        sectionRow.add(sectionCell(msg("matrix.route"), 1, "route"));
        sectionRow.add(sectionCell(msg("matrix.sectionAp"), 5, "section-ap"));
        sectionRow.add(sectionCell(msg("matrix.sectionUp"), 5, "section-up"));
        sectionRow.add(sectionCell(msg("matrix.sectionIbdr"), 1, "section-ibdr"));
        sectionRow.add(sectionCell(msg("matrix.sectionStatements"), 1, "section-statements"));
        matrix.add(sectionRow);

        Div headerRow = matrixRow(null);
        headerRow.add(headerCell(msg("matrix.route")));
        headerRow.add(headerCell("18.8"));
        headerRow.add(headerCell("20.1"));
        headerRow.add(headerCell("20.20"));
        headerRow.add(headerCell("20.21"));
        headerRow.add(headerCell(msg("matrix.other")));
        headerRow.add(headerCell(msg("matrix.fr")));
        headerRow.add(headerCell(msg("matrix.su")));
        headerRow.add(headerCell(msg("matrix.localSearch")));
        headerRow.add(headerCell(msg("matrix.identification")));
        headerRow.add(headerCell(msg("matrix.crime")));
        headerRow.add(headerCell(msg("matrix.ibdr")));
        headerRow.add(headerCell(msg("matrix.statements")));
        matrix.add(headerRow);

        for (RouteDetailsRow row : rows) {
            Div dataRow = matrixRow(null);
            dataRow.add(routeCell(row.routeLabel()));
            dataRow.add(numCell(row.ap188()));
            dataRow.add(numCell(row.ap201()));
            dataRow.add(numCell(row.ap2020()));
            dataRow.add(numCell(row.ap2021()));
            dataRow.add(numCell(row.apOther()));
            dataRow.add(numCell(row.upFederalWanted()));
            dataRow.add(numCell(row.upWatchList()));
            dataRow.add(numCell(row.upLocalSearch()));
            dataRow.add(numCell(row.upIdentification()));
            dataRow.add(numCell(row.upHotPursuit()));
            dataRow.add(numCell(row.ibdr()));
            dataRow.add(numCell(row.statements()));
            matrix.add(dataRow);
        }
        return matrix;
    }

    private Div buildPeriodPanel(TodayShiftDashboardSnapshot snapshot) {
        Div panel = uiComponents.create(Div.class);
        panel.addClassName("today-shift-dashboard__panel");
        panel.addClassName("today-shift-dashboard__period-panel");

        String departmentNumber = snapshot.departmentNumber() <= 0
                ? "—"
                : String.valueOf(snapshot.departmentNumber());
        H3 heading = uiComponents.create(H3.class);
        heading.setText(MessageFormat.format(
                msg("periodSummaryWithDepartment"),
                departmentNumber
        ));
        heading.addClassName("today-shift-dashboard__panel-title");
        heading.addClassName("today-shift-dashboard__period-title");
        panel.add(heading);

        Div matrixWrap = uiComponents.create(Div.class);
        matrixWrap.addClassName("today-shift-dashboard__period-matrix-wrap");
        matrixWrap.add(buildPeriodMatrix(snapshot.periodMetrics()));
        panel.add(matrixWrap);
        return panel;
    }

    private Div buildPeriodMatrix(List<PeriodMetricRow> rows) {
        Div matrix = uiComponents.create(Div.class);
        matrix.addClassName("today-shift-dashboard__matrix");
        matrix.addClassName("today-shift-dashboard__matrix--period");

        Div headerRow = matrixRow("today-shift-dashboard__period-header-row");
        headerRow.add(periodHeaderCell(msg("period.indicator")));
        headerRow.add(periodHeaderCell(msg("period.shift")));
        headerRow.add(periodHeaderCell(msg("period.month")));
        headerRow.add(periodHeaderCell(msg("period.year")));
        headerRow.add(periodHeaderCell(msg("period.total"), true));
        matrix.add(headerRow);

        for (PeriodMetricRow row : rows) {
            if (row.sectionHeader()) {
                Div section = matrixRow("today-shift-dashboard__period-section-row");
                Div label = uiComponents.create(Div.class);
                label.setText(msg(row.messageKey()));
                label.addClassName("today-shift-dashboard__matrix-cell");
                label.addClassName("today-shift-dashboard__matrix-section-label");
                section.add(label);
                matrix.add(section);
            } else {
                Div dataRow = matrixRow(row.sumRow() ? "today-shift-dashboard__period-sum-row" : null);
                dataRow.add(labelCell(
                        msg(row.messageKey()),
                        row.sumRow()
                ));
                dataRow.add(periodNumCell(row.shiftCount(), row.sumRow()));
                dataRow.add(periodNumCell(row.monthCount(), row.sumRow()));
                dataRow.add(periodNumCell(row.yearCount(), row.sumRow()));
                dataRow.add(periodTotalNumCell(row.totalCount(), row.sumRow()));
                matrix.add(dataRow);
            }
        }
        return matrix;
    }

    private Div matrixRow(String extraClass) {
        Div row = uiComponents.create(Div.class);
        row.addClassName("today-shift-dashboard__matrix-row");
        if (extraClass != null) {
            row.addClassName(extraClass);
        }
        return row;
    }

    private Div sectionCell(String text, int span, String sectionModifier) {
        Div cell = uiComponents.create(Div.class);
        cell.setText(text);
        cell.addClassName("today-shift-dashboard__matrix-cell");
        cell.addClassName("today-shift-dashboard__matrix-section-th");
        cell.addClassName("today-shift-dashboard__matrix-section-th--" + sectionModifier);
        if (span > 1) {
            cell.getStyle().set("grid-column", "span " + span);
        }
        return cell;
    }

    private Div headerCell(String text) {
        return headerCell(text, false);
    }

    private Div headerCell(String text, boolean totalColumn) {
        Div cell = uiComponents.create(Div.class);
        cell.setText(text);
        cell.addClassName("today-shift-dashboard__matrix-cell");
        cell.addClassName("today-shift-dashboard__matrix-th");
        if (totalColumn) {
            cell.addClassName("today-shift-dashboard__matrix-th--total");
        }
        return cell;
    }

    private Div periodHeaderCell(String text) {
        return periodHeaderCell(text, false);
    }

    private Div periodHeaderCell(String text, boolean totalColumn) {
        Div cell = headerCell(text, totalColumn);
        cell.addClassName("today-shift-dashboard__matrix-th--period");
        return cell;
    }

    private Div routeCell(String text) {
        Div cell = uiComponents.create(Div.class);
        cell.setText(text == null || text.isBlank() ? "—" : text);
        cell.addClassName("today-shift-dashboard__matrix-cell");
        cell.addClassName("today-shift-dashboard__matrix-route");
        return cell;
    }

    private Div labelCell(String text) {
        return labelCell(text, false);
    }

    private Div labelCell(String text, boolean bold) {
        Div cell = uiComponents.create(Div.class);
        cell.setText(text);
        cell.addClassName("today-shift-dashboard__matrix-cell");
        cell.addClassName("today-shift-dashboard__matrix-label");
        if (bold) {
            cell.addClassName("today-shift-dashboard__matrix-label--bold");
        }
        return cell;
    }

    private Div numCell(int value) {
        Div cell = uiComponents.create(Div.class);
        cell.setText(String.valueOf(value));
        cell.addClassName("today-shift-dashboard__matrix-cell");
        cell.addClassName("today-shift-dashboard__matrix-num");
        return cell;
    }

    private Div periodNumCell(int value, boolean bold) {
        Div cell = numCell(value);
        if (bold) {
            cell.addClassName("today-shift-dashboard__matrix-num--bold");
        }
        return cell;
    }

    private Div periodTotalNumCell(int value, boolean bold) {
        Div cell = numCell(value);
        cell.addClassName("today-shift-dashboard__matrix-num--total");
        if (bold) {
            cell.addClassName("today-shift-dashboard__matrix-num--bold");
        }
        return cell;
    }

    private Div narrowBreakdownPanel(String title, String emptyText, List<HorizontalLayout> rows) {
        Div panel = breakdownPanel(title, emptyText, rows);
        panel.addClassName("today-shift-dashboard__breakdown-panel--narrow");
        return panel;
    }

    private Div breakdownPanel(String title, String emptyText, List<HorizontalLayout> rows) {
        Div panel = uiComponents.create(Div.class);
        panel.addClassName("today-shift-dashboard__panel");
        panel.addClassName("today-shift-dashboard__breakdown-panel");

        H3 heading = uiComponents.create(H3.class);
        heading.setText(title);
        heading.addClassName("today-shift-dashboard__panel-title");
        heading.addClassName("today-shift-dashboard__panel-title--compact");
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
