package com.company.vzvod.view.education;

import com.company.vzvod.entity.Education;
import com.company.vzvod.service.EducationDialogSaveService;
import com.company.vzvod.service.EducationStatusService;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

@Route(value = "educations/:id", layout = MainView.class)
@ViewController(id = "Education.detail")
@ViewDescriptor(path = "education-detail-view.xml")
@EditedEntityContainer("educationDc")
public class EducationDetailView extends StandardDetailView<Education> {

    @ViewComponent
    private InstanceContainer<Education> educationDc;

    @ViewComponent
    private TypedDatePicker<LocalDate> untilField;

    @Autowired
    private EducationDialogSaveService educationDialogSaveService;

    @Autowired
    private EducationStatusService educationStatusService;

    @Subscribe
    public void onBeforeSave(final BeforeSaveEvent event) {
        Education education = getEditedEntity();
        educationStatusService.applyStatusFromUntil(education);

        event.preventSave();

        Education saved = educationDialogSaveService.saveFromDialog(education);

        DataContext parent = getViewData().getDataContext().getParent();
        if (parent != null) {
            parent.merge(saved);
            getViewData().getDataContext().clear();
            event.resume(close(StandardOutcome.SAVE));
            return;
        }

        getViewData().getDataContext().clear();
        educationDc.setItem(saved);
        clearChanges();
        event.resume(close(StandardOutcome.SAVE));
    }

    @Subscribe("untilField")
    public void onUntilFieldValueChange(
            AbstractField.ComponentValueChangeEvent<TypedDatePicker<LocalDate>, LocalDate> event) {
        Education education = educationDc.getItemOrNull();
        if (education != null) {
            educationStatusService.applyStatusFromUntil(education);
        }
    }
}
