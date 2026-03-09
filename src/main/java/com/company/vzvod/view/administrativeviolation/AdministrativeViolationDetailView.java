package com.company.vzvod.view.administrativeviolation;

import com.company.vzvod.entity.AdministrativeViolation;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "administrative-violations/:id", layout = MainView.class)
@ViewController(id = "AdministrativeViolation.detail")
@ViewDescriptor(path = "administrative-violation-detail-view.xml")
@EditedEntityContainer("administrativeViolationDc")
public class AdministrativeViolationDetailView extends StandardDetailView<AdministrativeViolation> {
}