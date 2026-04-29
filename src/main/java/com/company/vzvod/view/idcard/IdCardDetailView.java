package com.company.vzvod.view.idcard;

import com.company.vzvod.entity.IdCard;
import com.vaadin.flow.component.AbstractField;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

import java.time.LocalDate;

//@Route(value = "id-cards/:id", layout = MainViewTopMenu.class)
@ViewController(id = "IdCard.detail")
@ViewDescriptor(path = "id-card-detail-view.xml")
@EditedEntityContainer("idCardDc")
public class IdCardDetailView extends StandardDetailView<IdCard> {

    private static final int ID_CARD_VALID_YEARS = 5;

    @ViewComponent
    private TypedDatePicker<LocalDate> issuedField;

    @ViewComponent
    private TypedDatePicker<LocalDate> untilField;

    private boolean suppressAutoDates;

    @Subscribe("issuedField")
    public void onIssuedFieldComponentValueChange(
            final AbstractField.ComponentValueChangeEvent<TypedDatePicker<LocalDate>, LocalDate> event) {
        if (suppressAutoDates) {
            return;
        }

        LocalDate issued = event.getValue();
        if (issued == null) {
            return;
        }

        suppressAutoDates = true;
        try {
            untilField.setValue(issued.plusYears(ID_CARD_VALID_YEARS));
        } finally {
            suppressAutoDates = false;
        }
    }

    @Subscribe("untilField")
    public void onUntilFieldComponentValueChange(
            final AbstractField.ComponentValueChangeEvent<TypedDatePicker<LocalDate>, LocalDate> event) {
        if (suppressAutoDates) {
            return;
        }

        LocalDate until = event.getValue();
        if (until == null) {
            return;
        }

        suppressAutoDates = true;
        try {
            issuedField.setValue(until.minusYears(ID_CARD_VALID_YEARS));
        } finally {
            suppressAutoDates = false;
        }
    }
}