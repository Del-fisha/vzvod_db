package com.company.vzvod.view.administrativeviolation;

import com.company.vzvod.entity.AdministrativeViolation;
import com.company.vzvod.entity.Impact;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "administrative-violations/:id", layout = MainView.class)
@ViewController(id = "AdministrativeViolation.detail")
@ViewDescriptor(path = "administrative-violation-detail-view.xml")
@EditedEntityContainer("administrativeViolationDc")
public class AdministrativeViolationDetailView extends StandardDetailView<AdministrativeViolation> {

    @Subscribe
    public void onInitEntity(StandardDetailView.InitEntityEvent<AdministrativeViolation> event) {
        event.getEntity().setImpact(Impact.WITHOUT_IMPACT);
    }
}