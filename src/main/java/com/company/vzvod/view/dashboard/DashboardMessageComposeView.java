package com.company.vzvod.view.dashboard;

import com.company.vzvod.messaging.DashboardMessageAudience;
import com.company.vzvod.messaging.DashboardMessageSendService;
import com.company.vzvod.security.UiAccessService;
import com.company.vzvod.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

@Route(value = "dashboard-message", layout = MainView.class)
@ViewController(id = "DashboardMessageComposeView")
@ViewDescriptor(path = "dashboard-message-compose-view.xml")
public class DashboardMessageComposeView extends StandardView {

    private static final String MSG_PREFIX = "com.company.vzvod.view.dashboard/dashboardMessageComposeView.";

    @ViewComponent
    private JmixComboBox<DashboardMessageAudience> audienceField;

    @ViewComponent
    private JmixTextArea messageField;

    @ViewComponent
    private JmixButton sendButton;

    @Autowired
    private DashboardMessageSendService dashboardMessageSendService;

    @Autowired
    private UiAccessService uiAccessService;

    @Autowired
    private Messages messages;

    @Autowired
    private Notifications notifications;

    @Subscribe
    public void onInit(InitEvent event) {
        audienceField.setItems(DashboardMessageAudience.values());
        audienceField.setItemLabelGenerator(this::audienceLabel);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        if (!uiAccessService.hasFullAccessRole()) {
            throw new AccessDeniedException("Dashboard messaging requires FullAccessRole");
        }
    }

    @Subscribe("sendButton")
    public void onSendButtonClick(ClickEvent<JmixButton> event) {
        DashboardMessageAudience audience = audienceField.getValue();
        String message = messageField.getValue();
        if (audience == null || message == null || message.isBlank()) {
            notifications.create(messages.getMessage(MSG_PREFIX + "notification.emptyMessage"))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }

        try {
            dashboardMessageSendService.send(audience, message);
            notifications.create(messages.getMessage(MSG_PREFIX + "notification.sent"))
                    .withType(Notifications.Type.SUCCESS)
                    .show();
            messageField.clear();
        } catch (AccessDeniedException e) {
            notifications.create(messages.getMessage(MSG_PREFIX + "notification.accessDenied"))
                    .withType(Notifications.Type.ERROR)
                    .show();
        } catch (IllegalArgumentException e) {
            notifications.create(messages.getMessage(MSG_PREFIX + "notification.emptyMessage"))
                    .withType(Notifications.Type.WARNING)
                    .show();
        } catch (HttpStatusCodeException e) {
            notifications.create(messages.getMessage(
                            MSG_PREFIX + "notification.sendErrorWithStatus",
                            e.getStatusCode().toString()))
                    .withType(Notifications.Type.ERROR)
                    .show();
        } catch (RestClientException e) {
            notifications.create(messages.getMessage(MSG_PREFIX + "notification.serviceUnavailable"))
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    private String audienceLabel(DashboardMessageAudience audience) {
        if (audience == null) {
            return "";
        }
        return messages.getMessage(MSG_PREFIX + "audience." + audience.name());
    }
}
