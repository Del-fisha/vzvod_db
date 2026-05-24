package com.company.vzvod.view.criminalviolation;

import com.company.vzvod.entity.CriminalViolation;
import com.company.vzvod.entity.Impact;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "criminal-violations/:id", layout = MainView.class)
@ViewController(id = "CriminalViolation.detail")
@ViewDescriptor(path = "criminal-violation-detail-view.xml")
@EditedEntityContainer("criminalViolationDc")
public class CriminalViolationDetailView extends StandardDetailView<CriminalViolation> {

    @Subscribe
    public void onInitEntity(StandardDetailView.InitEntityEvent<CriminalViolation> event) {
        event.getEntity().setImpact(Impact.WITHOUT_IMPACT);
    }
}