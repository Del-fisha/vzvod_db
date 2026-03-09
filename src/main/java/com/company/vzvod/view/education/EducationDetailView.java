package com.company.vzvod.view.education;

import com.company.vzvod.entity.Education;
import com.company.vzvod.entity.EducationStatus;
import com.company.vzvod.view.main.MainView;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "educations/:id", layout = MainView.class)
@ViewController(id = "Education.detail")
@ViewDescriptor(path = "education-detail-view.xml")
@EditedEntityContainer("educationDc")
public class EducationDetailView extends StandardDetailView<Education> {
    @Subscribe
    public void onInitEntity(final InitEntityEvent<Education> event) {
        event.getEntity().setStatus(EducationStatus.FINISHED);
    }
}