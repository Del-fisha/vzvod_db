package com.company.vzvod.view.print;

import com.company.vzvod.dialog_beans.RaportMenuBean;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "print", layout = MainView.class)
@ViewController("PrintHubView")
@ViewDescriptor(path = "print-hub-view.xml")
public class PrintHubView extends StandardView {

    @ViewComponent
    private VerticalLayout printWidgetsSlot;

    @Autowired
    private RaportMenuBean raportMenuBean;

    /**
     * Показываем карточки при каждом открытии экрана: для навигационных {@code StandardView}
     * надёжнее, чем {@code InitEvent} (слот и инъекции гарантированно готовы).
     */
    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        if (printWidgetsSlot == null || raportMenuBean == null) {
            return;
        }
        printWidgetsSlot.removeAll();

        HomeCard otgul = new HomeCard(
                "Рапорт на отгул",
                "(Распечатать рапорт на отгул)",
                "Открыть",
                "var(--lumo-primary-color)"
        );
        otgul.addCardClickListener(() -> raportMenuBean.openCompensatoryRaport());
        printWidgetsSlot.add(otgul);

        HomeCard dailyShift = new HomeCard(
                "Перенос на сутки",
                "(Распечатать рапорт на перенос смен на сутки)",
                "Открыть",
                "var(--lumo-success-color)"
        );
        dailyShift.addCardClickListener(() -> raportMenuBean.openDailyShiftRaport());
        printWidgetsSlot.add(dailyShift);

        HomeCard serviceBook = new HomeCard(
                "Дополнение к служебной книжке",
                "(Корешки, опись, таблица телефонов, таблица позывных)",
                "Открыть",
                "var(--lumo-error-color)"
        );
        serviceBook.addCardClickListener(() -> raportMenuBean.openServiceBookSupplement());
        printWidgetsSlot.add(serviceBook);
    }

    @Tag("article")
    private static final class HomeCard extends HtmlComponent {
        private Runnable onClick;

        HomeCard(String title, String subtitle, String cta, String color) {
            addClassName("home-card");
            getElement().getStyle().set("--clr",
                    (color == null || color.isBlank()) ? "var(--lumo-primary-color)" : color);

            getElement().setProperty("innerHTML", """
                    <div class="home-card__icon" aria-hidden="true">🖨️</div>
                    <h3 class="home-card__title">%s</h3>
                    <div class="home-card__subtitle">%s</div>
                    <div class="home-card__cta" role="button" tabindex="0">%s</div>
                    """.formatted(escape(title), escape(subtitle), escape(cta)));

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

        private static String escape(String s) {
            if (s == null) {
                return "";
            }
            return s.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
        }
    }
}

