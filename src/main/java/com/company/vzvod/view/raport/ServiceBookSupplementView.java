package com.company.vzvod.view.raport;

import com.company.vzvod.entity.Post;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.StatusInService;
import com.company.vzvod.entity.User;
import com.company.vzvod.service.ServiceInfoVocationStatusService;
import com.company.vzvod.service.dto.raport.PersonDto;
import com.company.vzvod.service.dto.raport.ServiceBookSupplementDto;
import com.company.vzvod.service.raport.ServiceBookSupplementSender;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@ViewController("ServiceBookSupplementView")
@ViewDescriptor(path = "service-book-supplement-view.xml")
public class ServiceBookSupplementView extends StandardView {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Autowired
    private DataManager dataManager;

    @Autowired
    private Messages messages;

    @Autowired
    private Notifications notifications;

    @Autowired
    private ServiceBookSupplementSender sender;

    @Autowired
    private ServiceInfoVocationStatusService serviceInfoVocationStatusService;

    @Value("${raport.microservice.url}")
    private String raportMicroserviceUrl;

    @ViewComponent
    private EntityComboBox<User> employeeDept1Picker;

    @ViewComponent
    private EntityComboBox<User> employeeDept2Picker;

    @ViewComponent
    private EntityComboBox<User> signerPicker;

    @ViewComponent
    private TypedDatePicker<LocalDate> reportDateField;

    @Subscribe
    public void onInit(InitEvent event) {
        LocalDate today = LocalDate.now();
        reportDateField.setTypedValue(today);

        updateSignerForReportDate(today, true);
    }

    @Subscribe("reportDateField")
    public void onReportDateFieldValueChange(
            AbstractField.ComponentValueChangeEvent<TypedDatePicker<LocalDate>, LocalDate> event
    ) {
        updateSignerForReportDate(event.getValue(), false);
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
        send(false);
    }

    @Subscribe("sendAndPrintBtn")
    public void onSendAndPrintBtnClick(ClickEvent<JmixButton> event) {
        send(true);
    }

    private void send(boolean openPdf) {
        User employee = employeeDept1Picker.getValue();
        if (employee == null) {
            employee = employeeDept2Picker.getValue();
        }
        User signer = signerPicker.getValue();
        LocalDate reportDate = reportDateField.getTypedValue();

        if (employee == null || signer == null || reportDate == null) {
            notifications.create("Заполните все поля")
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }

        ServiceBookSupplementDto dto = new ServiceBookSupplementDto();
        dto.setEmployee(toPersonDto(employee));
        dto.setPetitioner(toPersonDto(signer));
        dto.setReportDate(reportDate.format(formatter));

        try {
            if (openPdf) {
                byte[] pdfBytes = sender.sendPdf(dto);
                openPdfInNewTab(pdfBytes, "service-book-" + UUID.randomUUID() + ".pdf");
            } else {
                sender.send(dto);
            }
            notifications.create("Документ отправлен")
                    .withType(Notifications.Type.SUCCESS)
                    .show();
            close(StandardOutcome.CLOSE);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            String msg = "Ошибка отправки: " + e.getStatusCode();
            if (e.getStatusCode().value() == 404) {
                msg += ". Эндпоинт не найден на " + raportMicroserviceUrl
                        + " — пересоберите и запустите raport-service с актуальным кодом"
                        + " и проверьте raport.microservice.url (обычно http://localhost:8082).";
            }
            notifications.create(msg)
                    .withType(Notifications.Type.ERROR)
                    .show();
        } catch (org.springframework.web.client.RestClientException e) {
            notifications.create("Сервис формирования документов недоступен")
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    private void openPdfInNewTab(byte[] bytes, String fileName) {
        if (bytes == null || bytes.length == 0) {
            notifications.create("Сервис формирования документов недоступен")
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
                .gender(null)
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

    private void updateSignerForReportDate(LocalDate reportDate, boolean force) {
        if (reportDate == null) {
            return;
        }

        // Если пользователь вручную выбрал подписанта, который не A/B — не перетираем.
        User current = signerPicker.getValue();
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
        signerPicker.setValue(commanderActive ? commander : deputy);
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
}

