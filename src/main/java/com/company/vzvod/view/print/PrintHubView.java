package com.company.vzvod.view.print;

import com.company.vzvod.dialog_beans.RaportMenuBean;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "print", layout = MainView.class)
@ViewController("PrintHubView")
@ViewDescriptor(path = "print-hub-view.xml")
public class PrintHubView extends StandardView {

    @ViewComponent
    private VerticalLayout printWidgetsSlot;

    @Autowired
    private RaportMenuBean raportMenuBean;

    @Subscribe
    public void onInit(InitEvent event) {
        if (printWidgetsSlot == null) {
            return;
        }
        printWidgetsSlot.removeAll();

        HomeCard otgul = new HomeCard(
                "Рапорт на отгул",
                "Открывает форму рапорта в диалоговом окне.",
                "Открыть",
                "var(--lumo-primary-color)"
        );
        otgul.addCardClickListener(() -> raportMenuBean.openCompensatoryRaport());
        printWidgetsSlot.add(otgul);

        HomeCard dailyShift = new HomeCard(
                "Перенос на сутки",
                "Открывает рапорт «На сутки» в диалоговом окне.",
                "Открыть",
                "var(--lumo-success-color)"
        );
        dailyShift.addCardClickListener(() -> raportMenuBean.openDailyShiftRaport());
        printWidgetsSlot.add(dailyShift);
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

