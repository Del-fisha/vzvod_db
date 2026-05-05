package com.company.vzvod.view.mainviewtopmenu;

import com.company.vzvod.view.dashboard.WorkResultsStatisticsDialog;
import com.google.common.base.Strings;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.app.main.StandardMainView;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;

@ViewController(id = "MainViewTopMenu")
@ViewDescriptor(path = "main-view-top-menu.xml")
public class MainViewTopMenu extends StandardMainView {

    @Autowired
    private DialogWindows dialogWindows;

    @ViewComponent
    private VerticalLayout homeStatsWidgetSlot;

    @Subscribe
    public void onInit(final InitEvent event) {
        installHomeStatsWidget();
    }

    private void installHomeStatsWidget() {
        if (homeStatsWidgetSlot == null) {
            return;
        }
        homeStatsWidgetSlot.removeAll();

        HomeStatsCard card = new HomeStatsCard(
                "Статистика результатов работы",
                "Откройте сводную статистику по работе за выбранный период.",
                "Открыть",
                "var(--lumo-primary-color)"
        );
        card.addCardClickListener(() -> {
            DialogWindow<WorkResultsStatisticsDialog> w = dialogWindows.view(this, WorkResultsStatisticsDialog.class).build();
            w.open();
        });
        homeStatsWidgetSlot.add(card);
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