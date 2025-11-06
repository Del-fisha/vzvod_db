package com.company.vzvod.view.education;

import com.company.vzvod.entity.Education;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "educations/:id", layout = MainViewTopMenu.class)
@ViewController(id = "Education.detail")
@ViewDescriptor(path = "education-detail-view.xml")
@EditedEntityContainer("educationDc")
public class EducationDetailView extends StandardDetailView<Education> {
}