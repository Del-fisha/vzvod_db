package com.company.vzvod.view.raport;

import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import com.company.vzvod.service.dto.raport.FinancialAssistanceRaportDto;
import com.company.vzvod.service.dto.raport.PersonDto;
import com.company.vzvod.service.raport.FinancialAssistanceRaportSender;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@ViewController("FinancialAssistanceRaportView")
@ViewDescriptor(value = "financial-assistance-raport-view.xml", path = "financial-assistance-raport-view.xml")
public class FinancialAssistanceRaportView extends StandardView {

    private static final String MSG_PREFIX = "com.company.vzvod.view.raport/financialAssistanceRaportView.";

    @Autowired
    private Messages messages;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private Notifications notifications;

    @Autowired
    private FinancialAssistanceRaportSender raportSender;

    @ViewComponent
    private JmixMultiSelectComboBox<User> employeeDept1Picker;

    @ViewComponent
    private JmixMultiSelectComboBox<User> employeeDept2Picker;

    @ViewComponent
    private JmixComboBox<String> commanderComboBox;

    @ViewComponent
    private TypedDatePicker<LocalDate> reportDateField;

    @ViewComponent
    private TypedTextField<String> yearField;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final String OOOP_DEPUTY_FULL_DEFAULT =
            "Зам. начальника ОООП Самусенко Виктор Александрович подполковник";
    private static final String OOOP_DEPUTY_PREFIX_DEFAULT = "Зам. начальника ОООП";
    private static final String OOOP_DEPUTY_POSITION_DEFAULT = "Зам. начальника ОООП";

    @Subscribe
    public void onInit(InitEvent event) {
        LocalDate today = LocalDate.now();
        reportDateField.setTypedValue(today);
        yearField.setTypedValue(Integer.toString(today.getYear()));

        String ooopDeputyFull = msgOrDefault(MSG_PREFIX + "commander.ooopDeputy.full", OOOP_DEPUTY_FULL_DEFAULT);
        commanderComboBox.setItems(
                messages.getMessage(MSG_PREFIX + "commander.chief.full"),
                messages.getMessage(MSG_PREFIX + "commander.deputy.full"),
                ooopDeputyFull,
                messages.getMessage(MSG_PREFIX + "commander.actingChief.full")
        );
    }

    @Subscribe("sendBtn")
    public void onSendBtnClick(ClickEvent<JmixButton> event) {
        send(false);
    }

    @Subscribe("sendAndPrintBtn")
    public void onSendAndPrintBtnClick(ClickEvent<JmixButton> event) {
        send(true);
    }

    private void send(boolean openPdf) {
        List<User> employees = selectedEmployees();
        String commanderStr = commanderComboBox.getValue();
        LocalDate reportDate = reportDateField.getTypedValue();
        String year = yearField.getTypedValue();

        if (employees.isEmpty() || commanderStr == null || reportDate == null || year == null || year.isBlank()) {
            notifications.create(messages.getMessage(MSG_PREFIX + "validation.fillAllFields"))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }

        FinancialAssistanceRaportDto raport = new FinancialAssistanceRaportDto();
        raport.setEmployees(employees.stream().map(this::toPersonDto).toList());
        raport.setRecipient(fromCommanderString(commanderStr));
        raport.setReportDate(reportDate.format(formatter));
        raport.setYear(year.trim());

        try {
            if (openPdf) {
                byte[] pdfBytes = raportSender.sendFinancialAssistancePdf(raport);
                openPdfInNewTab(pdfBytes, "financial-assistance-" + UUID.randomUUID() + ".pdf");
            } else {
                raportSender.sendFinancialAssistance(raport);
            }
            notifications.create(messages.getMessage(MSG_PREFIX + "notification.sent"))
                    .withType(Notifications.Type.SUCCESS)
                    .show();
            close(StandardOutcome.CLOSE);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            String status = e.getStatusCode().toString();
            notifications.create(messages.getMessage(MSG_PREFIX + "notification.sendErrorWithStatus", status))
                    .withType(Notifications.Type.ERROR)
                    .show();
        } catch (org.springframework.web.client.RestClientException e) {
            notifications.create(messages.getMessage(MSG_PREFIX + "notification.serviceUnavailable"))
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    private void openPdfInNewTab(byte[] bytes, String fileName) {
        if (bytes == null || bytes.length == 0) {
            notifications.create(messages.getMessage(MSG_PREFIX + "notification.serviceUnavailable"))
                    .withType(Notifications.Type.ERROR)
                    .show();
            return;
        }

        StreamResource resource = new StreamResource(fileName, () -> new ByteArrayInputStream(bytes));
        resource.setContentType("application/pdf");

        StreamRegistration registration = VaadinSession.getCurrent()
                .getResourceRegistry()
                .registerResource(resource);

        UI.getCurrent().getPage().open(registration.getResourceUri().toString(), "_blank");
    }

    private PersonDto toPersonDto(User user) {
        User fresh = loadUserForDto(user);
        if (fresh == null) {
            return PersonDto.builder().build();
        }

        ServiceInfo serviceInfo = fresh.getServiceInfo();

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
                .firstName(fresh.getFirstName())
                .lastName(fresh.getLastName())
                .middleName(fresh.getPatronymic())
                .rank(rank)
                .position(position)
                .gender("MALE")
                .build();
    }

    private User loadUserForDto(User user) {
        if (user == null || user.getId() == null) {
            return null;
        }
        return dataManager.load(User.class)
                .id(user.getId())
                .fetchPlan(fp -> fp
                        .add("firstName")
                        .add("lastName")
                        .add("patronymic")
                        .add("serviceInfo", si -> si.add("rank").add("post"))
                )
                .one();
    }

    private PersonDto fromCommanderString(String s) {
        String position;
        String lastName;
        String firstName;
        String middleName;
        String rank;

        String chiefPrefix = messages.getMessage(MSG_PREFIX + "commander.chief.prefix");
        String deputyPrefix = messages.getMessage(MSG_PREFIX + "commander.deputy.prefix");
        String actingChiefPrefix = messages.getMessage(MSG_PREFIX + "commander.actingChief.prefix");
        String ooopDeputyPrefix = msgOrDefault(MSG_PREFIX + "commander.ooopDeputy.prefix", OOOP_DEPUTY_PREFIX_DEFAULT);

        String sNorm = normalizePrefixLike(s);
        String chiefNorm = normalizePrefixLike(chiefPrefix);
        String deputyNorm = normalizePrefixLike(deputyPrefix);
        String actingChiefNorm = normalizePrefixLike(actingChiefPrefix);
        String ooopDeputyNorm = normalizePrefixLike(ooopDeputyPrefix);

        if (sNorm.startsWith(ooopDeputyNorm)) {
            String[] parts = s.split("\\s+");
            position = msgOrDefault(MSG_PREFIX + "commander.ooopDeputy.position", OOOP_DEPUTY_POSITION_DEFAULT);
            lastName = parts[3];
            firstName = parts[4];
            middleName = parts[5];
            rank = parts[6];
        } else if (sNorm.startsWith(actingChiefNorm)) {
            String[] parts = s.split("\\s+");
            position = messages.getMessage(MSG_PREFIX + "commander.actingChief.position");
            lastName = parts[2];
            firstName = parts[3];
            middleName = parts[4];
            rank = parts[5];
        } else if (sNorm.startsWith(deputyNorm)) {
            String[] parts = s.split("\\s+");
            position = messages.getMessage(MSG_PREFIX + "commander.deputy.position");
            lastName = parts[2];
            firstName = parts[3];
            middleName = parts[4];
            rank = parts[5];
        } else if (sNorm.startsWith(chiefNorm)) {
            String[] parts = s.split("\\s+");
            position = messages.getMessage(MSG_PREFIX + "commander.chief.position");
            lastName = parts[1];
            firstName = parts[2];
            middleName = parts[3];
            rank = parts[4];
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

    private static String normalizePrefixLike(String v) {
        if (v == null) {
            return "";
        }
        return v.toLowerCase()
                .replace(".", "")
                .replace(" ", "")
                .trim();
    }

    private String msgOrDefault(String key, String defaultValue) {
        String v = messages.getMessage(key);
        return key.equals(v) ? defaultValue : v;
    }

    private List<User> selectedEmployees() {
        List<User> employees = new ArrayList<>();
        addAll(employees, employeeDept1Picker.getValue());
        addAll(employees, employeeDept2Picker.getValue());
        employees.sort(Comparator
                .comparing((User user) -> user.getLastName() == null ? "" : user.getLastName(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(user -> user.getFirstName() == null ? "" : user.getFirstName(), String.CASE_INSENSITIVE_ORDER));
        return employees;
    }

    private static void addAll(List<User> target, Set<User> selected) {
        if (selected != null) {
            target.addAll(selected);
        }
    }
}

