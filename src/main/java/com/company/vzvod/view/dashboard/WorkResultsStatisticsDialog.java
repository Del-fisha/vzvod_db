package com.company.vzvod.view.dashboard;

import com.company.vzvod.dashboard.stats.*;
import com.company.vzvod.entity.ArticleOfAdministrative;
import com.company.vzvod.entity.TypeOfCriminal;
import com.company.vzvod.service.DashboardStatisticsService;
import com.company.vzvod.service.DashboardStatisticsService.AdministrativeArticleOptionDto;
import com.company.vzvod.service.DashboardStatisticsService.CriminalTypeOptionDto;
import com.company.vzvod.service.DashboardStatisticsService.DepartmentEmployeesRow;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@Route(value = "work-results-statistics", layout = MainView.class)
@ViewController(id = "WorkResultsStatisticsDialog")
@ViewDescriptor(path = "work-results-statistics-dialog.xml")
@DialogMode(width = "min(1200px, 96vw)", height = "min(880px, 92vh)")
public class WorkResultsStatisticsDialog extends StandardView {

    @ViewComponent
    private VerticalLayout rootContainer;

    @Autowired
    private DashboardStatisticsService dashboardStatisticsService;

    @Autowired
    private Notifications notifications;

    @Autowired
    private MessageBundle messageBundle;

    @Autowired
    private Messages messages;

    @Autowired
    private UiComponents uiComponents;

    private final Map<UUID, Checkbox> departmentCheckboxes = new LinkedHashMap<>();
    private final Map<UUID, Checkbox> employeeCheckboxes = new LinkedHashMap<>();

    private RadioButtonGroup<StatsPeriod> periodGroup;
    private Tabs metricTabs;
    private WorkMetric selectedMetric = WorkMetric.ADMINISTRATIVE_VIOLATIONS;

    private HorizontalLayout subMenuRow;
    private HorizontalLayout articleFiltersRow;
    private HorizontalLayout criminalFiltersRow;
    private Div ibdrLabel;
    private final List<Checkbox> articleCheckboxes = new ArrayList<>();
    private final List<Integer> articleOptionIds = new ArrayList<>();
    private final List<Checkbox> criminalCheckboxes = new ArrayList<>();
    private final List<Integer> criminalOptionIds = new ArrayList<>();
    private Span totalsLine;
    private VerticalLayout chartArea;
    private VerticalLayout departmentTreeBox;

    @Subscribe
    public void onInit(InitEvent event) {
        buildMainLayout();
        loadDepartmentTree();
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        refreshChart();
    }

    private void buildMainLayout() {
        rootContainer.removeAll();
        rootContainer.setSpacing(true);
        rootContainer.setPadding(true);
        rootContainer.setSizeFull();

        chartArea = uiComponents.create(VerticalLayout.class);
        chartArea.setSpacing(true);
        chartArea.setPadding(true);
        chartArea.setWidthFull();
        chartArea.addClassName("work-results-chart-area");
        chartArea.getStyle()
                .set("border-radius", "12px")
                .set("background", "linear-gradient(180deg, var(--lumo-contrast-5pct), transparent)");

        HorizontalLayout row = uiComponents.create(HorizontalLayout.class);
        row.setWidthFull();
        row.setSpacing(true);
        row.setAlignItems(FlexComponent.Alignment.START);

        Scroller leftScroller = uiComponents.create(Scroller.class);
        leftScroller.setWidth("340px");
        leftScroller.setMinWidth("300px");
        leftScroller.setHeight("720px");
        leftScroller.getElement().getStyle().set("flex-shrink", "0");
        departmentTreeBox = uiComponents.create(VerticalLayout.class);
        departmentTreeBox.setSpacing(true);
        departmentTreeBox.setPadding(true);
        leftScroller.setContent(departmentTreeBox);

        VerticalLayout controls = controlsColumn();
        row.add(leftScroller, controls);
        row.expand(controls);
        rootContainer.add(row);
    }

    private VerticalLayout controlsColumn() {
        VerticalLayout col = uiComponents.create(VerticalLayout.class);
        col.setSpacing(true);
        col.setPadding(false);
        col.setWidthFull();

        periodGroup = new RadioButtonGroup<>();
        periodGroup.setLabel(messageBundle.getMessage("workResultsStatisticsDialog.periodLabel"));
        periodGroup.setItems(StatsPeriod.TODAY, StatsPeriod.MONTH, StatsPeriod.YEAR, StatsPeriod.ALL_TIME);
        periodGroup.setItemLabelGenerator(this::periodLabel);
        periodGroup.setValue(StatsPeriod.MONTH);
        periodGroup.addValueChangeListener(e -> refreshChart());

        metricTabs = new Tabs();
        metricTabs.setWidthFull();
        Tab adminTab = new Tab(messageBundle.getMessage("workResultsStatisticsDialog.metricAdministrative"));
        Tab criminalTab = new Tab(messageBundle.getMessage("workResultsStatisticsDialog.metricCriminal"));
        Tab ibdrTab = new Tab(messageBundle.getMessage("workResultsStatisticsDialog.metricIbdr"));
        metricTabs.add(adminTab, criminalTab, ibdrTab);
        metricTabs.setSelectedTab(adminTab);
        metricTabs.addSelectedChangeListener(e -> {
            Tab t = e.getSelectedTab();
            if (t == adminTab) {
                selectedMetric = WorkMetric.ADMINISTRATIVE_VIOLATIONS;
            } else if (t == criminalTab) {
                selectedMetric = WorkMetric.CRIMINAL_VIOLATIONS;
            } else {
                selectedMetric = WorkMetric.IBDR;
            }
            updateSubMenuVisibility();
            refreshChart();
        });

        // Submenu area (horizontal, depends on selected metric)
        subMenuRow = uiComponents.create(HorizontalLayout.class);
        subMenuRow.setSpacing(true);
        subMenuRow.setWidthFull();

        articleFiltersRow = uiComponents.create(HorizontalLayout.class);
        articleFiltersRow.setSpacing(true);
        articleFiltersRow.setWidthFull();
        articleFiltersRow.getStyle().set("flex-wrap", "wrap");

        articleCheckboxes.clear();
        articleOptionIds.clear();
        for (AdministrativeArticleOptionDto o : dashboardStatisticsService.allAdministrativeArticleOptions()) {
            ArticleOfAdministrative a = ArticleOfAdministrative.fromId(o.id());
            Checkbox cb = new Checkbox(a == null ? o.enumName() : entityCaption(a));
            cb.setValue(true);
            cb.addValueChangeListener(ev -> refreshChart());
            articleCheckboxes.add(cb);
            articleOptionIds.add(o.id());
            articleFiltersRow.add(cb);
        }

        criminalFiltersRow = uiComponents.create(HorizontalLayout.class);
        criminalFiltersRow.setSpacing(true);
        criminalFiltersRow.setWidthFull();
        criminalFiltersRow.getStyle().set("flex-wrap", "wrap");

        criminalCheckboxes.clear();
        criminalOptionIds.clear();
        for (CriminalTypeOptionDto o : dashboardStatisticsService.allCriminalTypeOptions()) {
            TypeOfCriminal t = TypeOfCriminal.fromId(o.id());
            Checkbox cb = new Checkbox(t == null ? o.enumName() : entityCaption(t));
            cb.setValue(true);
            cb.addValueChangeListener(ev -> refreshChart());
            criminalCheckboxes.add(cb);
            criminalOptionIds.add(o.id());
            criminalFiltersRow.add(cb);
        }

        ibdrLabel = uiComponents.create(Div.class);
        ibdrLabel.setText(messageBundle.getMessage("workResultsStatisticsDialog.ibdrOnly"));
        ibdrLabel.getStyle().set("opacity", "0.9");

        subMenuRow.add(articleFiltersRow, criminalFiltersRow, ibdrLabel);
        updateSubMenuVisibility();

        totalsLine = uiComponents.create(Span.class);
        totalsLine.getStyle().set("white-space", "pre-wrap");

        JmixButton applyBtn = uiComponents.create(JmixButton.class);
        applyBtn.setText(messageBundle.getMessage("workResultsStatisticsDialog.apply"));
        applyBtn.addThemeName("primary");
        applyBtn.addClickListener(ev -> refreshChart());

        JmixButton closeBtn = uiComponents.create(JmixButton.class);
        closeBtn.setText(messageBundle.getMessage("workResultsStatisticsDialog.close"));
        closeBtn.addClickListener(ev -> close(StandardOutcome.DISCARD));

        HorizontalLayout actions = uiComponents.create(HorizontalLayout.class);
        actions.setSpacing(true);
        actions.add(applyBtn, closeBtn);

        col.add(periodGroup, metricTabs, subMenuRow, totalsLine, chartArea, actions);
        col.expand(chartArea);
        return col;
    }

    private void loadDepartmentTree() {
        departmentTreeBox.removeAll();
        departmentCheckboxes.clear();
        employeeCheckboxes.clear();
        departmentTreeBox.add(new H3(messageBundle.getMessage("workResultsStatisticsDialog.departmentsTitle")));

        for (DepartmentEmployeesRow row : dashboardStatisticsService.loadDepartmentEmployeeTree()) {
            Integer deptNum = row.departmentNumber();
            String deptNumLabel = (deptNum == null || deptNum <= 0) ? "—" : deptNum.toString();
            String deptPattern = messageBundle.getMessage("workResultsStatisticsDialog.departmentNumber");
            Checkbox deptCb = new Checkbox(MessageFormat.format(deptPattern, deptNumLabel));
            UUID deptId = row.departmentId();
            departmentCheckboxes.put(deptId, deptCb);
            deptCb.addValueChangeListener(e -> {
                if (Boolean.TRUE.equals(e.getValue())) {
                    row.employeesByUserId().keySet().forEach(uid ->
                            Optional.ofNullable(employeeCheckboxes.get(uid)).ifPresent(c -> c.setValue(false)));
                }
            });

            VerticalLayout empCol = uiComponents.create(VerticalLayout.class);
            empCol.setSpacing(false);
            empCol.setPadding(false);
            empCol.getStyle().set("padding-left", "var(--lumo-space-l)");
            for (Map.Entry<UUID, String> en : row.employeesByUserId().entrySet()) {
                Checkbox ec = new Checkbox(en.getValue());
                employeeCheckboxes.put(en.getKey(), ec);
                ec.addValueChangeListener(ev -> {
                    if (Boolean.TRUE.equals(ev.getValue())) {
                        deptCb.setValue(false);
                    }
                });
                empCol.add(ec);
            }

            VerticalLayout box = uiComponents.create(VerticalLayout.class);
            box.setSpacing(true);
            box.setPadding(false);
            box.add(deptCb, empCol);
            departmentTreeBox.add(box);
        }

        // Default: show comparison for all departments on first open.
        if (!departmentCheckboxes.isEmpty()
                && departmentCheckboxes.values().stream().noneMatch(Checkbox::getValue)
                && employeeCheckboxes.values().stream().noneMatch(Checkbox::getValue)) {
            departmentCheckboxes.values().forEach(cb -> cb.setValue(true));
        }
    }

    private void updateSubMenuVisibility() {
        boolean admin = selectedMetric == WorkMetric.ADMINISTRATIVE_VIOLATIONS;
        boolean criminal = selectedMetric == WorkMetric.CRIMINAL_VIOLATIONS;
        boolean ibdr = selectedMetric == WorkMetric.IBDR;
        articleFiltersRow.setVisible(admin);
        criminalFiltersRow.setVisible(criminal);
        ibdrLabel.setVisible(ibdr);
    }

    private void refreshChart() {
        try {
            StatsQuery query = buildQuery();
            query.validate();
            StatsResult result = dashboardStatisticsService.loadStats(query);
            renderTotals(result, query);
            renderChart(result);
        } catch (IllegalArgumentException ex) {
            notifications.create(ex.getMessage())
                    .withType(Notifications.Type.WARNING)
                    .show();
        }
    }

    private StatsQuery buildQuery() {
        EnumSet<WorkMetric> metrics = EnumSet.of(selectedMetric);

        Set<UUID> selEmp = employeeCheckboxes.entrySet().stream()
                .filter(e -> Boolean.TRUE.equals(e.getValue().getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<UUID> selDept = departmentCheckboxes.entrySet().stream()
                .filter(e -> Boolean.TRUE.equals(e.getValue().getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        StatsPeriod period = periodGroup.getValue();
        if (period == null) {
            period = StatsPeriod.MONTH;
        }

        if (!selEmp.isEmpty()) {
            Set<Integer> artIds = selectedIds(articleCheckboxes, articleOptionIds, selectedMetric == WorkMetric.ADMINISTRATIVE_VIOLATIONS);
            Set<Integer> crIds = selectedIds(criminalCheckboxes, criminalOptionIds, selectedMetric == WorkMetric.CRIMINAL_VIOLATIONS);
            return new StatsQuery(
                    period,
                    null,
                    metrics,
                    artIds,
                    crIds,
                    StatsCompareMode.EMPLOYEES,
                    Set.of(),
                    selEmp
            );
        }

        Set<Integer> artIds = selectedIds(articleCheckboxes, articleOptionIds, selectedMetric == WorkMetric.ADMINISTRATIVE_VIOLATIONS);
        Set<Integer> crIds = selectedIds(criminalCheckboxes, criminalOptionIds, selectedMetric == WorkMetric.CRIMINAL_VIOLATIONS);
        return new StatsQuery(
                period,
                null,
                metrics,
                artIds,
                crIds,
                StatsCompareMode.DEPARTMENTS,
                selDept,
                Set.of()
        );
    }

    private Set<Integer> selectedIds(List<Checkbox> boxes, List<Integer> ids, boolean sectionActive) {
        if (!sectionActive || boxes.isEmpty() || ids.size() != boxes.size()) {
            return Set.of();
        }
        Set<Integer> all = new HashSet<>(ids);
        Set<Integer> on = new HashSet<>();
        for (int i = 0; i < boxes.size(); i++) {
            if (Boolean.TRUE.equals(boxes.get(i).getValue())) {
                on.add(ids.get(i));
            }
        }
        if (on.size() == all.size()) {
            return Set.of();
        }
        return on;
    }

    private void renderTotals(StatsResult result, StatsQuery query) {
        EmployeePeriodTotals t = result.employeeTotalsOrNull();
        if (t == null) {
            if (query.period() == StatsPeriod.TODAY) {
                totalsLine.setText(messageBundle.getMessage("workResultsStatisticsDialog.totalsTodayHidden"));
            } else if (query.compareMode() == StatsCompareMode.EMPLOYEES && query.employeeUserIds().size() != 1) {
                totalsLine.setText(messageBundle.getMessage("workResultsStatisticsDialog.totalsNeedOneEmployee"));
            } else {
                totalsLine.setText("");
            }
            return;
        }
        long v = switch (selectedMetric) {
            case ADMINISTRATIVE_VIOLATIONS -> t.administrativeViolations();
            case CRIMINAL_VIOLATIONS -> t.criminalViolations();
            case IBDR -> t.ibdr();
        };
        String totalsPattern = messageBundle.getMessage("workResultsStatisticsDialog.totalsLineSingle");
        totalsLine.setText(MessageFormat.format(totalsPattern, metricLabel(selectedMetric), v));
    }

    private void renderChart(StatsResult result) {
        chartArea.removeAll();
        if (result.bucketLabels().isEmpty() || result.series().isEmpty()) {
            chartArea.add(new Span(messageBundle.getMessage("workResultsStatisticsDialog.noChartData")));
            return;
        }

        String[] colors = {"var(--lumo-primary-color)", "var(--lumo-success-color)", "var(--lumo-error-color)",
                "var(--lumo-contrast-40pct)"};

        HorizontalLayout legend = uiComponents.create(HorizontalLayout.class);
        legend.setSpacing(true);
        legend.setWidthFull();
        int ci = 0;
        for (StatsSeries s : result.series()) {
            Span leg = new Span("● " + s.label());
            leg.getStyle().set("color", colors[ci++ % colors.length]);
            legend.add(leg);
        }
        chartArea.add(legend);

        chartArea.add(buildLineChartSvg(result, colors));
    }

    /** Целевая интервал между подписями по Y (линии сетки = подписи включая ноль сверху). */
    private static final int CHART_Y_DIVISION_TARGET = 5;

    private Div buildLineChartSvg(StatsResult result, String[] colors) {
        int buckets = result.bucketLabels().size();
        int w = 920;
        int h = 360;

        double dataMax = result.series().stream()
                .flatMapToDouble(s -> Arrays.stream(s.bucketValues()))
                .max()
                .orElse(1.0);
        if (!(dataMax > 0)) {
            dataMax = 1.0;
        }

        double approxStep = dataMax / Math.max(CHART_Y_DIVISION_TARGET - 1, 1);
        long tickStep = Math.max(1L, (long) Math.ceil(niceChartIncrement(Math.max(approxStep, 1e-15))));
        double axisTop = tickStep * Math.ceil(dataMax / tickStep);
        if (!(axisTop > 0)) {
            axisTop = tickStep;
        }

        int padR = 14;
        int padT = 18;
        int padB = 38;
        int padL = 16 + chartYLabelReserve(axisTop, tickStep);

        double innerW = Math.max(1, w - padL - padR);
        double innerH = Math.max(1, h - padT - padB);
        double stepX = buckets <= 1 ? 0 : innerW / (buckets - 1);

        double yBase = padT + innerH;
        double yTop = padT;

        StringBuilder sb = new StringBuilder();
        sb.append("<svg viewBox='0 0 ").append(w).append(" ").append(h).append("' ")
                .append("width='100%' height='").append(h).append("'>");

        // horizontal grid + Y axis labels (low values at bottom → high at top)
        for (double yValue = 0; yValue <= axisTop + tickStep * 1e-9; yValue += tickStep) {
            double frac = axisTop <= 1e-12 ? 0 : Math.min(Math.max(yValue / axisTop, 0), 1);
            double gridY = yBase - frac * innerH;
            boolean onBottomAxis = Math.abs(gridY - yBase) < 0.5;
            if (!onBottomAxis) {
                sb.append("<line x1='").append(padL).append("' y1='").append(String.format(Locale.US, "%.2f", gridY))
                        .append("' x2='").append(padL + innerW).append("' y2='")
                        .append(String.format(Locale.US, "%.2f", gridY))
                        .append("' stroke='var(--lumo-contrast-10pct)' stroke-width='1' stroke-dasharray='4 4'/>");
            }

            sb.append("<text x='").append(padL - 6)
                    .append("' y='").append(String.format(Locale.US, "%.2f", gridY + 4))
                    .append("' font-size='12' text-anchor='end' fill='var(--lumo-secondary-text-color)'>")
                    .append(escapeXml(formatYAxisLabel(yValue)))
                    .append("</text>");
        }

        // Y axis spine
        sb.append("<line x1='").append(padL).append("' y1='").append(String.format(Locale.US, "%.2f", yBase))
                .append("' x2='").append(padL).append("' y2='").append(String.format(Locale.US, "%.2f", yTop))
                .append("' stroke='var(--lumo-contrast-25pct)' stroke-width='1'/>");

        // X axis baseline
        sb.append("<line x1='").append(padL).append("' y1='").append(String.format(Locale.US, "%.2f", yBase))
                .append("' x2='").append(padL + innerW).append("' y2='").append(String.format(Locale.US, "%.2f", yBase))
                .append("' stroke='var(--lumo-contrast-30pct)' stroke-width='1'/>");

        // x labels (sparse)
        int every = Math.max(1, buckets / 8);
        for (int i = 0; i < buckets; i += every) {
            double x = padL + stepX * i;
            sb.append("<text x='").append(x).append("' y='").append(h - 12)
                    .append("' font-size='12' text-anchor='middle' fill='var(--lumo-secondary-text-color)'>")
                    .append(escapeXml(result.bucketLabels().get(i)))
                    .append("</text>");
        }

        int si = 0;
        for (StatsSeries series : result.series()) {
            String color = colors[si++ % colors.length];
            StringBuilder pts = new StringBuilder();
            for (int i = 0; i < buckets; i++) {
                double v = series.bucketValues()[i];
                double x = padL + stepX * i;
                double fracY = axisTop <= 1e-12 ? 0 : Math.min(Math.max(v / axisTop, 0), 1);
                double y = padT + innerH - fracY * innerH;
                if (i > 0) {
                    pts.append(' ');
                }
                pts.append(String.format(Locale.US, "%.2f,%.2f", x, y));
            }
            sb.append("<polyline fill='none' stroke='").append(color)
                    .append("' stroke-width='3' stroke-linejoin='round' stroke-linecap='round' ")
                    .append("points='").append(pts).append("'/>");

            for (int i = 0; i < buckets; i++) {
                double v = series.bucketValues()[i];
                double x = padL + stepX * i;
                double fracY = axisTop <= 1e-12 ? 0 : Math.min(Math.max(v / axisTop, 0), 1);
                double y = padT + innerH - fracY * innerH;
                sb.append("<circle cx='").append(String.format(Locale.US, "%.2f", x))
                        .append("' cy='").append(String.format(Locale.US, "%.2f", y))
                        .append("' r='3.5' fill='").append(color).append("'/>");
            }
        }
        sb.append("</svg>");

        Div box = uiComponents.create(Div.class);
        box.getElement().setProperty("innerHTML", sb.toString());
        box.getStyle()
                .set("border-radius", "12px")
                .set("padding", "8px")
                .set("background", "color-mix(in srgb, var(--lumo-base-color) 70%, transparent)");
        return box;
    }

    private static double niceChartIncrement(double approxStep) {
        if (!(approxStep > 0)) {
            return 1;
        }
        double exp = Math.floor(Math.log10(approxStep));
        double mantissa = approxStep / Math.pow(10, exp);
        double nf = mantissa <= 1 ? 1 : mantissa <= 2 ? 2 : mantissa <= 5 ? 5 : 10;
        return nf * Math.pow(10, exp);
    }

    private static int chartYLabelReserve(double axisTop, long tickStep) {
        int maxChars = formatYAxisLabel(0).length();
        int guard = 0;
        for (double yVal = 0; yVal <= axisTop + tickStep * 1e-9 && guard < 512; yVal += tickStep, guard++) {
            maxChars = Math.max(maxChars, formatYAxisLabel(yVal).length());
        }
        return Math.min(130, Math.max(32, maxChars * 8 + 6));
    }

    private static String formatYAxisLabel(double v) {
        // По оси Y — только целые значения (счётчики событий).
        return String.valueOf((long) Math.round(v));
    }

    private String metricLabel(WorkMetric m) {
        return switch (m) {
            case ADMINISTRATIVE_VIOLATIONS -> messageBundle.getMessage("workResultsStatisticsDialog.metricAdministrative");
            case CRIMINAL_VIOLATIONS -> messageBundle.getMessage("workResultsStatisticsDialog.metricCriminal");
            case IBDR -> messageBundle.getMessage("workResultsStatisticsDialog.metricIbdr");
        };
    }

    private static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    private String periodLabel(StatsPeriod p) {
        return switch (p) {
            case TODAY -> messageBundle.getMessage("workResultsStatisticsDialog.period.today");
            case MONTH -> messageBundle.getMessage("workResultsStatisticsDialog.period.month");
            case YEAR -> messageBundle.getMessage("workResultsStatisticsDialog.period.year");
            case ALL_TIME -> messageBundle.getMessage("workResultsStatisticsDialog.period.allTime");
        };
    }

    private String entityCaption(ArticleOfAdministrative article) {
        return messages.getMessage(article);
    }

    private String entityCaption(TypeOfCriminal type) {
        return messages.getMessage(type);
    }
}
