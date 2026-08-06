package com.company.vzvod.view.mainviewtopmenu;

import com.company.vzvod.security.UiAccessService;
import com.company.vzvod.view.dashboard.DashboardMessageComposeView;
import com.company.vzvod.view.dashboard.WorkResultsStatisticsDialog;
import com.company.vzvod.view.dashboard.TodayShiftDashboardView;
import com.google.common.base.Strings;
import com.vaadin.flow.component.UI;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.app.main.StandardMainView;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.company.vzvod.view.userlist.UserListView;
import com.company.vzvod.view.event.EventListView;
import com.company.vzvod.view.print.PrintHubView;
import io.jmix.core.Messages;
import org.springframework.beans.factory.annotation.Autowired;

@ViewController(id = "MainViewTopMenu")
@ViewDescriptor(path = "main-view-top-menu.xml")
public class MainViewTopMenu extends StandardMainView {

    @Autowired
    private DialogWindows dialogWindows;

    @Autowired
    private Messages messages;

    @Autowired
    private UiAccessService uiAccessService;

    @ViewComponent
    private VerticalLayout homeStatsWidgetSlot;

    @Subscribe
    public void onInit(final InitEvent event) {
        installHomeStatsWidget();
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        installHomeStatsWidget();
    }

    private void installHomeStatsWidget() {
        if (homeStatsWidgetSlot == null) {
            return;
        }
        homeStatsWidgetSlot.removeAll();

        HomeStatsCard stats = new HomeStatsCard(
                "Статистика результатов работы",
                "Выберите период, сотрудников и элементы для получения статистики",
                "Открыть",
                "var(--lumo-primary-color)"
        );
        stats.addCardClickListener(() -> {
            DialogWindow<WorkResultsStatisticsDialog> w = dialogWindows.view(this, WorkResultsStatisticsDialog.class).build();
            w.open();
        });
        homeStatsWidgetSlot.add(stats);

        HomeStatsCard todayShift = new HomeStatsCard(
                messages.getMessage("com.company.vzvod.view.main/openTodayShiftDashboardBtn.text"),
                messages.getMessage("com.company.vzvod.view.main/openTodayShiftDashboardBtn.subtitle"),
                messages.getMessage("com.company.vzvod.view.main/openTodayShiftDashboardBtn.cta"),
                "var(--lumo-contrast-50pct)"
        );
        todayShift.addCardClickListener(() -> UI.getCurrent().navigate(TodayShiftDashboardView.class));
        homeStatsWidgetSlot.add(todayShift);

        if (uiAccessService.hasFullAccessRole()) {
            HomeStatsCard dashboardMessage = new HomeStatsCard(
                    messages.getMessage("com.company.vzvod.view.main/openDashboardMessageBtn.text"),
                    messages.getMessage("com.company.vzvod.view.main/openDashboardMessageBtn.subtitle"),
                    messages.getMessage("com.company.vzvod.view.main/openDashboardMessageBtn.cta"),
                    "var(--lumo-secondary-color)"
            );
            dashboardMessage.addCardClickListener(() -> UI.getCurrent().navigate(DashboardMessageComposeView.class));
            homeStatsWidgetSlot.add(dashboardMessage);
        }

        HomeStatsCard employees = new HomeStatsCard(
                "Все сотрудники",
                "Общая база данных всех сотрудников",
                "Открыть",
                "var(--lumo-success-color)"
        );
        employees.addCardClickListener(() -> UI.getCurrent().navigate(UserListView.class));
        homeStatsWidgetSlot.add(employees);

        HomeStatsCard events = new HomeStatsCard(
                "Мероприятия",
                "Планируемые, прошедшие и «без взвода» — всё в одном месте.",
                "Открыть",
                "var(--lumo-error-color)"
        );
        events.addCardClickListener(() -> UI.getCurrent().navigate(EventListView.class));
        homeStatsWidgetSlot.add(events);

        HomeStatsCard print = new HomeStatsCard(
                "Распечатать",
                "Рапорта и другие документы для быстрой распечатки с актуальными данными",
                "Открыть",
                "var(--lumo-primary-text-color)"
        );
        print.addCardClickListener(() -> UI.getCurrent().navigate(PrintHubView.class));
        homeStatsWidgetSlot.add(print);
    }

    @Override
    protected void updateTitle() {
        super.updateTitle();

        String viewTitle = getTitleFromOpenedView();
        UiComponentUtils.findComponent(getContent(), "viewHeaderBox")
                .ifPresent(component -> component.setVisible(!Strings.isNullOrEmpty(viewTitle)));
    }

    @Tag("article")
    private static final class HomeStatsCard extends HtmlComponent {
        private Runnable onClick;

        HomeStatsCard(String title, String subtitle, String cta, String color) {
            addClassName("home-card");
            getElement().getStyle().set("--clr", (color == null || color.isBlank()) ? "var(--lumo-primary-color)" : color);
            getElement().setProperty("innerHTML", buildInnerHtml(
                    escapeHtml(title),
                    escapeHtml(subtitle),
                    escapeHtml(cta)
            ));
            getElement().addEventListener("click", e -> {
                if (onClick != null) {
                    onClick.run();
                }
            });
            getElement().getStyle().set("cursor", "pointer");
        }

        void addCardClickListener(Runnable r) {
            this.onClick = r;
        }

        private static String buildInnerHtml(String title, String subtitle, String cta) {
            return """
                    <div class="home-card__icon" aria-hidden="true">📊</div>
                    <h3 class="home-card__title">%s</h3>
                    <div class="home-card__subtitle">%s</div>
                    <div class="home-card__cta" role="button" tabindex="0">%s</div>
                    """.formatted(title, subtitle, cta);
        }

        private static String escapeHtml(String s) {
            if (s == null) {
                return "";
            }
            return s
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
        }
    }
}