package com.company.vzvod.view.user;

import com.company.vzvod.entity.User;
import com.company.vzvod.mobile.MobileAuthService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.textfield.PasswordField;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@ViewController("ChangePasswordView")
@ViewDescriptor("change-password-view.xml")
public class ChangePasswordView extends StandardView {

    private User user;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Notifications notifications;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private Messages messages;

    @Autowired
    private MobileAuthService mobileAuthService;

    @ViewComponent
    private PasswordField oldPasswordField;
    @ViewComponent
    private PasswordField newPasswordField;
    @ViewComponent
    private PasswordField confirmPasswordField;

    public void setUser(User user) {
        this.user = user;
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        if (user == null) {
            notifications.create(messages.getMessage("com.company.vzvod.view.user/changePasswordView.userNotDefined"))
                    .show();
            close(StandardOutcome.CLOSE);
            return;
        }

        boolean passwordAlreadySet = user.getPassword() != null && !user.getPassword().isBlank();
        oldPasswordField.setVisible(passwordAlreadySet);
        oldPasswordField.setRequired(passwordAlreadySet);
        if (!passwordAlreadySet) {
            oldPasswordField.clear();
        }
    }

    @Subscribe("saveButton")
    public void onSaveButtonClick(ClickEvent<JmixButton> event) {
        if (user == null) {
            notifications.create(messages.getMessage("com.company.vzvod.view.user/changePasswordView.userNotDefined"))
                    .show();
            return;
        }

        String oldPass = oldPasswordField.getValue();
        String newPass = newPasswordField.getValue();
        String confirm = confirmPasswordField.getValue();

        if (newPass == null || newPass.isBlank()) {
            notifications.create(messages.getMessage("com.company.vzvod.view.user/changePasswordView.newPasswordRequired"))
                    .show();
            return;
        }
        if (!newPass.equals(confirm)) {
            notifications.create(messages.getMessage("com.company.vzvod.view.user/changePasswordView.passwordsDoNotMatch"))
                    .show();
            return;
        }

        String existingHash = user.getPassword();
        boolean passwordAlreadySet = existingHash != null && !existingHash.isBlank();

        if (passwordAlreadySet) {
            if (oldPass == null || oldPass.isBlank() || !passwordEncoder.matches(oldPass, existingHash)) {
                notifications.create(messages.getMessage("com.company.vzvod.view.user/changePasswordView.oldPasswordIncorrect"))
                        .show();
                return;
            }
        }

        user.setPassword(passwordEncoder.encode(newPass));
        dataManager.save(user);
        mobileAuthService.revokeBindingsForUser(user.getId());

        close(StandardOutcome.SAVE);
    }

    @Subscribe("cancelButton")
    public void onCancelButtonClick(ClickEvent<JmixButton> event) {
        close(StandardOutcome.CLOSE);
    }
}