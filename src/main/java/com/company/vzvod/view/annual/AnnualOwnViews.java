package com.company.vzvod.view.annual;

import com.company.vzvod.annual.AnnualSection;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

abstract class AnnualOwnView extends StandardView {

    @ViewComponent
    private VerticalLayout body;

    @Autowired
    private AnnualScreens screens;

    protected abstract AnnualSection section();

    @Subscribe
    public void onReady(ReadyEvent event) {
        render();
    }

    private void render() {
        screens.showOwn(body, section(), this::render);
    }
}

@Route(value = "annual/profosmotr", layout = MainView.class)
@ViewController("AnnualProfView")
@ViewDescriptor("annual-section-view.xml")
class AnnualProfView extends AnnualOwnView {
    @Override
    protected AnnualSection section() {
        return AnnualSection.PROFOSMOTR;
    }
}

@Route(value = "annual/cpd", layout = MainView.class)
@ViewController("AnnualCpdView")
@ViewDescriptor("annual-section-view.xml")
class AnnualCpdView extends AnnualOwnView {
    @Override
    protected AnnualSection section() {
        return AnnualSection.CPD;
    }
}

@Route(value = "annual/tests", layout = MainView.class)
@ViewController("AnnualTestsView")
@ViewDescriptor("annual-section-view.xml")
class AnnualTestsView extends AnnualOwnView {
    @Override
    protected AnnualSection section() {
        return AnnualSection.TESTS;
    }
}

@Route(value = "annual/psychologist", layout = MainView.class)
@ViewController("AnnualPsychView")
@ViewDescriptor("annual-section-view.xml")
class AnnualPsychView extends AnnualOwnView {
    @Override
    protected AnnualSection section() {
        return AnnualSection.PSYCHOLOGIST;
    }
}
