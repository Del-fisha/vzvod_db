package com.company.vzvod.view.incentive;

import com.company.vzvod.entity.Incentive;
import com.company.vzvod.entity.Initiator;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.security.UiAccessService;
import com.company.vzvod.service.IncentiveBulkCreateService;
import com.vaadin.flow.component.ClickEvent;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.core.validation.group.UiCrossFieldChecks;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

import java.util.Set;

@ViewController(id = "Incentive.bulk")
@ViewDescriptor(path = "incentive-bulk-detail-view.xml")
@DialogMode(width = "64em")
public class IncentiveBulkDetailView extends StandardView {

    private static final String MSG_PREFIX = "com.company.vzvod.view.incentive/";

    @ViewComponent
    private InstanceContainer<Incentive> incentiveDc;

    @ViewComponent
    private JmixMultiSelectComboBoxPicker<ServiceInfo> usersPicker;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private IncentiveBulkCreateService incentiveBulkCreateService;

    @Autowired
    private UiAccessService uiAccessService;

    @Autowired
    private Validator validator;

    @Autowired
    private Messages messages;

    @Autowired
    private Notifications notifications;

    private ServiceInfo preselectedServiceInfo;

    public void setPreselectedServiceInfo(ServiceInfo preselectedServiceInfo) {
        this.preselectedServiceInfo = preselectedServiceInfo;
    }

    @Subscribe
    public void onInit(InitEvent event) {
        Incentive incentive = dataManager.create(Incentive.class);
        incentive.setInitiator(Initiator.METRO);
        incentiveDc.setItem(incentive);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        if (!uiAccessService.hasFullAccessRole()) {
            throw new AccessDeniedException("Bulk incentive creation requires FullAccessRole");
        }
        if (preselectedServiceInfo != null && preselectedServiceInfo.getId() != null) {
            ServiceInfo merged = dataManager.load(ServiceInfo.class)
                    .id(preselectedServiceInfo.getId())
                    .one();
            usersPicker.setValue(Set.of(merged));
        }
    }

    @Subscribe("saveAndCloseButton")
    public void onSaveAndCloseButtonClick(ClickEvent<JmixButton> event) {
        Incentive template = incentiveDc.getItem();
        Set<ServiceInfo> selectedUsers = usersPicker.getValue();

        if (selectedUsers == null || selectedUsers.isEmpty()) {
            notifications.create(messages.getMessage(MSG_PREFIX + "incentiveBulkDetailView.users.required"))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }

        Set<ConstraintViolation<Incentive>> violations = validator.validate(template, UiCrossFieldChecks.class);
        if (!violations.isEmpty()) {
            ConstraintViolation<Incentive> first = violations.iterator().next();
            notifications.create(first.getMessage())
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }

        incentiveBulkCreateService.createForServiceInfos(template, selectedUsers);
        close(StandardOutcome.SAVE);
    }
}
