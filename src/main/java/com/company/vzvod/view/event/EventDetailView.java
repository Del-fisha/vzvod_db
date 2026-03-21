package com.company.vzvod.view.event;

import com.company.vzvod.entity.Event;
import com.company.vzvod.entity.EventType;
import com.company.vzvod.service.DepartmentConverter;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

import java.time.LocalDate;
import java.util.Arrays;

@Route(value = "events/:id", layout = MainView.class)
@ViewController(id = "Event.detail")
@ViewDescriptor(path = "event-detail-view.xml")
@EditedEntityContainer("eventDc")
public class EventDetailView extends StandardDetailView<Event> {

    @Subscribe("dateField")
    public void onDateFieldTypedValueChange(
            SupportsTypedValue.TypedValueChangeEvent<TypedDatePicker<LocalDate>,
                    LocalDate> event) {

        LocalDate date = event.getValue();

        int shift = DepartmentConverter.departmentFromDateToInt(date);

        getEditedEntity().setShiftOfDepartment(shift);
    }

    @Subscribe
    public void onInitEntity(InitEntityEvent<Event> event) {
        event.getEntity().setEventType(EventType.OTHER);
    }

    @Subscribe("nameField")
    public void onNameFieldTypedValueChange(
            SupportsTypedValue.TypedValueChangeEvent<TypedDatePicker<String>, String> event) {

        String[] sportWords = {"Зенит", "Ска", "Драконы", "Dragons", "Россия"};
        String name = event.getValue();

        assert name != null;

        boolean isSportTeam = Arrays.stream(sportWords)
                .anyMatch(word -> name.toLowerCase().contains(word.toLowerCase()));

        if (isSportTeam) {
            getEditedEntity().setEventType(EventType.SPORT);
        }
    }

}