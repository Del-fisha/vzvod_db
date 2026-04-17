package com.company.vzvod.view.raport;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import com.company.vzvod.service.dto.raport.CompensatoryTimeRaportDto;
import com.company.vzvod.service.dto.raport.PersonDto;
import com.company.vzvod.service.raport.CompensatoryTimeRaportSender;
import com.vaadin.flow.component.ClickEvent;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

@ViewController("CompensatoryTimeRaportView")
@ViewDescriptor(value = "compensatory-time-raport-view.xml", path = "compensatory-time-raport-view.xml")
public class CompensatoryTimeRaportView extends StandardView {

    @Autowired
    private Messages messages;

    @ViewComponent
    private EntityComboBox<User> employeeUserPicker;

    @ViewComponent
    private EntityComboBox<User> intercederUserPicker;

    @ViewComponent
    private JmixComboBox<String> recipientComboBox;

    @ViewComponent
    private TypedDatePicker<LocalDate> reportDateField;

    @ViewComponent
    private TypedDatePicker<LocalDate> dayOffDateField;

    @Autowired
    private CompensatoryTimeRaportSender raportSender;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private Notifications notifications;

    @Subscribe
    public void onInit(InitEvent event) {
        recipientComboBox.setItems(
                "Начальник Платов Михаил Викторович Полковник",
                "Врио начальника Бахуров Михаил Иванович Полковник"
        );
    }

    @Subscribe("sendBtn")
    public void onSendBtnClick(ClickEvent<JmixButton> event) {
        User employee = employeeUserPicker.getValue();
        User interceder = intercederUserPicker.getValue();
        String recipientStr = recipientComboBox.getValue();
        LocalDate reportDate = reportDateField.getTypedValue();
        LocalDate dayOffDate = dayOffDateField.getTypedValue();

        if (employee == null || interceder == null || recipientStr == null
                || reportDate == null || dayOffDate == null) {
            notifications.create("Заполните все поля")
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }
        employee = dataManager.load(User.class)
                .id(employee.getId())
                .one();
        interceder = dataManager.load(Id.of(interceder)).one();

        PersonDto employeeDto = toPersonDto(employee);
        PersonDto intercederDto = toPersonDto(interceder);
        PersonDto recipientDto = fromRecipientString(recipientStr);

        CompensatoryTimeRaportDto raport = new CompensatoryTimeRaportDto();
        raport.setEmployee(employeeDto);
        raport.setInterceder(intercederDto);
        raport.setRecipient(recipientDto);
        raport.setReportDate(reportDate.toString());
        raport.setDayOffDate(dayOffDate.toString());

        try {
            raportSender.sendOtgulRaport(raport);

            notifications.create("Рапорт отправлен")
                    .withType(Notifications.Type.SUCCESS)
                    .show();
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            String status = e.getStatusCode().toString();
            String body = e.getResponseBodyAsString();

            notifications.create("Ошибка при отправке рапорта: " + status)
                    .withType(Notifications.Type.ERROR)
                    .show();
        } catch (org.springframework.web.client.RestClientException e) {
            notifications.create("Не удалось связаться с сервисом рапортов")
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    private PersonDto toPersonDto(User user) {
        ServiceInfo serviceInfo = user.getServiceInfo();

        String rank = null;
        String position = null;

        if (serviceInfo != null) {
            if (serviceInfo.getRank() != null) {
                rank = messages.getMessage(serviceInfo.getRank());
            }
            if (serviceInfo.getPost() != null) {
                position = messages.getMessage(serviceInfo.getPost());
            }
        }

        return PersonDto.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .middleName(user.getPatronymic())
                .rank(rank)
                .position(position)
                .gender("MALE")
                .build();
    }

    private PersonDto fromRecipientString(String s) {
        String position;
        String lastName;
        String firstName;
        String middleName;
        String rank;

        if (s.startsWith("Начальник ")) {
            String[] parts = s.split("\\s+");
            position = "Начальник";
            lastName = parts[1];
            firstName = parts[2];
            middleName = parts[3];
            rank = parts[4];
        } else if (s.startsWith("Врио начальника ")) {
            String[] parts = s.split("\\s+");
            position = "Врио начальника";
            lastName = parts[2];
            firstName = parts[3];
            middleName = parts[4];
            rank = parts[5];
        } else {
            position = s;
            lastName = s;
            firstName = "UNKNOWN";
            middleName = null;
            rank = null;
        }

        return PersonDto.builder()
                .firstName(firstName)
                .lastName(lastName)
                .middleName(middleName)
                .rank(rank)
                .position(position)
                .gender(null)
                .build();
    }
}