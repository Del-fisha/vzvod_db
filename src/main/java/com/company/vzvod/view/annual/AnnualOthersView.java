package com.company.vzvod.view.annual;

import com.company.vzvod.security.UiAccessService;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "annual/others", layout = MainView.class)
@ViewController("AnnualOthersView")
@ViewDescriptor("annual-section-view.xml")
public class AnnualOthersView extends StandardView {

    @ViewComponent
    private VerticalLayout body;

    @Autowired
    private AnnualScreens screens;

    @Autowired
    private UiAccessService uiAccessService;

    private final AnnualScreens.OthersState state = new AnnualScreens.OthersState();

    @Subscribe
    public void onReady(ReadyEvent event) {
        state.rerender = this::render;
        render();
    }

    private void render() {
        screens.showOthers(body, uiAccessService.hasFullAccessRole(), state);
    }
}
