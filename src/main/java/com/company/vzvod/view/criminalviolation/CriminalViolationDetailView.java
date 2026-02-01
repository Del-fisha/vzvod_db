package com.company.vzvod.view.criminalviolation;

import com.company.vzvod.entity.CriminalViolation;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "criminal-violations/:id", layout = MainViewTopMenu.class)
@ViewController(id = "CriminalViolation.detail")
@ViewDescriptor(path = "criminal-violation-detail-view.xml")
@EditedEntityContainer("criminalViolationDc")
public class CriminalViolationDetailView extends StandardDetailView<CriminalViolation> {
}