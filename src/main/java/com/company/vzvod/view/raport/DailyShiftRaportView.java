package com.company.vzvod.view.raport;

import com.company.vzvod.entity.Post;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.StatusInService;
import com.company.vzvod.entity.User;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.service.DepartmentConverter;
import com.company.vzvod.service.ServiceInfoVocationStatusService;
import com.company.vzvod.service.dto.raport.DailyShiftRaportDto;
import com.company.vzvod.service.dto.raport.PersonDto;
import com.company.vzvod.service.raport.DailyShiftRaportSender;
import com.vaadin.flow.component.AbstractField;
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
import java.time.format.DateTimeFormatter;

@ViewController("DailyShiftRaportView")
@ViewDescriptor(value = "daily-shift-raport-view.xml", path = "daily-shift-raport-view.xml")
public class DailyShiftRaportView extends StandardView {

    private static final String MSG_PREFIX = "com.company.vzvod.view.raport/dailyShiftRaportView.";

    @Autowired
    private Messages messages;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private Notifications notifications;

    @Autowired
    private ServiceInfoVocationStatusService serviceInfoVocationStatusService;

    @Autowired
    private DailyShiftRaportSender raportSender;

    @ViewComponent
    private EntityComboBox<User> employeeDept1Picker;

    @ViewComponent
    private EntityComboBox<User> employeeDept2Picker;

    @ViewComponent
    private EntityComboBox<User> petitionerUserPicker;

    @ViewComponent
    private JmixComboBox<String> commanderComboBox;

    @ViewComponent
    private TypedDatePicker<LocalDate> reportDateField;

    @ViewComponent
    private TypedDatePicker<LocalDate> firstTimeDateField;

    @ViewComponent
    private TypedDatePicker<LocalDate> secondTimeDateField;

    @ViewComponent
    private TypedDatePicker<LocalDate> newTimeDateField;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final String OOOP_DEPUTY_FULL_DEFAULT =
            "Зам. начальника ОООП Самусенко Виктор Александрович подполковник";
    private static final String OOOP_DEPUTY_PREFIX_DEFAULT = "Зам. начальника ОООП";
    private static final String OOOP_DEPUTY_POSITION_DEFAULT = "Зам. начальника ОООП";

    @Subscribe
    public void onInit(InitEvent event) {
        LocalDate today = LocalDate.now();
        reportDateField.setTypedValue(today);
        firstTimeDateField.setTypedValue(today);
        secondTimeDateField.setTypedValue(today);
        newTimeDateField.setTypedValue(today);

        String ooopDeputyFull = msgOrDefault(MSG_PREFIX + "commander.ooopDeputy.full", OOOP_DEPUTY_FULL_DEFAULT);
        commanderComboBox.setItems(
                messages.getMessage(MSG_PREFIX + "commander.chief.full"),
                messages.getMessage(MSG_PREFIX + "commander.deputy.full"),
                ooopDeputyFull
        );

        updatePetitionerForReportDate(today, true);
    }

    @Subscribe("reportDateField")
    public void onReportDateFieldValueChange(
            AbstractField.ComponentValueChangeEvent<TypedDatePicker<LocalDate>, LocalDate> event) {
        updatePetitionerForReportDate(event.getValue(), false);
    }

    @Subscribe("employeeDept1Picker")
    public void onEmployeeDept1PickerValueChange(
            AbstractField.ComponentValueChangeEvent<EntityComboBox<User>, User> event) {
        if (event.getValue() != null) {
            employeeDept2Picker.clear();
            employeeDept2Picker.setEnabled(false);
        } else {
            employeeDept2Picker.setEnabled(true);
        }
    }

    @Subscribe("employeeDept2Picker")
    public void onEmployeeDept2PickerValueChange(
            AbstractField.ComponentValueChangeEvent<EntityComboBox<User>, User> event) {
        if (event.getValue() != null) {
            employeeDept1Picker.clear();
            employeeDept1Picker.setEnabled(false);
        } else {
            employeeDept1Picker.setEnabled(true);
        }
    }

    @Subscribe("sendBtn")
    public void onSendBtnClick(ClickEvent<JmixButton> event) {
        User employee = employeeDept1Picker.getValue();
        if (employee == null) {
            employee = employeeDept2Picker.getValue();
        }

        User petitioner = petitionerUserPicker.getValue();
        String commanderStr = commanderComboBox.getValue();
        LocalDate reportDate = reportDateField.getTypedValue();
        LocalDate firstTimeDate = firstTimeDateField.getTypedValue();
        LocalDate secondTimeDate = secondTimeDateField.getTypedValue();
        LocalDate newTimeDate = newTimeDateField.getTypedValue();

        if (employee == null || petitioner == null || commanderStr == null
                || reportDate == null || firstTimeDate == null || secondTimeDate == null || newTimeDate == null) {
            notifications.create(messages.getMessage(MSG_PREFIX + "validation.fillAllFields"))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }

        employee = dataManager.load(User.class)
                .id(employee.getId())
                .fetchPlan(fp -> fp
                        .add("firstName")
                        .add("lastName")
                        .add("patronymic")
                        .add("serviceInfo", si -> si.add("rank").add("post"))
                )
                .one();
        petitioner = dataManager.load(User.class)
                .id(petitioner.getId())
                .fetchPlan(fp -> fp
                        .add("firstName")
                        .add("lastName")
                        .add("patronymic")
                        .add("serviceInfo", si -> si.add("rank").add("post"))
                )
                .one();

        if (!hasShiftOnDate(employee, firstTimeDate)) {
            notifications.create(messages.getMessage(MSG_PREFIX + "validation.noShiftForDate",
                            firstTimeDate.format(formatter)))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }
        if (!hasShiftOnDate(employee, secondTimeDate)) {
            notifications.create(messages.getMessage(MSG_PREFIX + "validation.noShiftForDate",
                            secondTimeDate.format(formatter)))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }

        DailyShiftRaportDto raport = new DailyShiftRaportDto();
        raport.setEmployee(toPersonDto(employee));
        raport.setPetitioner(toPersonDto(petitioner));
        raport.setRecipient(fromCommanderString(commanderStr));
        raport.setReportDate(reportDate.format(formatter));
        raport.setFirstTimeDate(firstTimeDate.format(formatter));
        raport.setSecondTimeDate(secondTimeDate.format(formatter));
        raport.setNewTimeDate(newTimeDate.format(formatter));

        try {
            raportSender.sendDailyShiftRaport(raport);
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

    private boolean hasShiftOnDate(User employee, LocalDate date) {
        if (employee == null || date == null) {
            return false;
        }
        ServiceInfo si = employee.getServiceInfo();
        if (si == null || si.getId() == null) {
            return false;
        }
        int dep = DepartmentConverter.departmentFromDateToInt(date);
        Long cnt = dataManager.loadValue(
                        "select count(s) from Shift s join s.units u " +
                                "where u.id = :serviceInfoId and s.date = :date and s.departmentToday = :dep",
                        Long.class
                )
                .parameter("serviceInfoId", si.getId())
                .parameter("date", date)
                .parameter("dep", dep)
                .one();
        return cnt != null && cnt > 0;
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
        String ooopDeputyPrefix = msgOrDefault(MSG_PREFIX + "commander.ooopDeputy.prefix", OOOP_DEPUTY_PREFIX_DEFAULT);

        if (s.startsWith(chiefPrefix + " ")) {
            String[] parts = s.split("\\s+");
            position = messages.getMessage(MSG_PREFIX + "commander.chief.position");
            lastName = parts[1];
            firstName = parts[2];
            middleName = parts[3];
            rank = parts[4];
        } else if (s.startsWith(deputyPrefix + " ")) {
            String[] parts = s.split("\\s+");
            position = messages.getMessage(MSG_PREFIX + "commander.deputy.position");
            lastName = parts[2];
            firstName = parts[3];
            middleName = parts[4];
            rank = parts[5];
        } else if (s.startsWith(ooopDeputyPrefix + " ")) {
            String[] parts = s.split("\\s+");
            position = msgOrDefault(MSG_PREFIX + "commander.ooopDeputy.position", OOOP_DEPUTY_POSITION_DEFAULT);
            lastName = parts[3];
            firstName = parts[4];
            middleName = parts[5];
            rank = parts[6];
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

    private String msgOrDefault(String key, String defaultValue) {
        String v = messages.getMessage(key);
        return key.equals(v) ? defaultValue : v;
    }

    private void updatePetitionerForReportDate(LocalDate reportDate, boolean force) {
        if (reportDate == null) {
            return;
        }

        User current = petitionerUserPicker.getValue();
        if (!force && current != null) {
            ServiceInfo si = current.getServiceInfo();
            Post p = si == null ? null : si.getPost();
            if (p != Post.COM_VZVOD && p != Post.ZAM_COM_VZVOD) {
                return;
            }
        }

        User commander = loadUserByPost(Post.COM_VZVOD);
        User deputy = loadUserByPost(Post.ZAM_COM_VZVOD);
        if (commander == null && deputy == null) {
            return;
        }

        boolean commanderActive = isUserActiveOnDate(commander, reportDate);
        petitionerUserPicker.setValue(commanderActive ? commander : deputy);
    }

    private boolean isUserActiveOnDate(User user, LocalDate date) {
        if (user == null || date == null) {
            return false;
        }
        ServiceInfo serviceInfo = user.getServiceInfo();
        if (serviceInfo == null || serviceInfo.getId() == null) {
            return false;
        }

        ServiceInfo fresh = dataManager.load(ServiceInfo.class)
                .id(serviceInfo.getId())
                .fetchPlan(fp -> fp.add("status"))
                .optional()
                .orElse(null);
        if (fresh == null) {
            return false;
        }

        if (fresh.getStatus() != StatusInService.ACTIVE) {
            return false;
        }

        return !serviceInfoVocationStatusService.hasVocationToday(fresh.getId(), date);
    }

    private User loadUserByPost(Post post) {
        if (post == null) {
            return null;
        }
        return dataManager.load(User.class)
                .query("select u from User u join u.serviceInfo si where si.post = :post")
                .parameter("post", post.getId())
                .optional()
                .orElse(null);
    }
}

