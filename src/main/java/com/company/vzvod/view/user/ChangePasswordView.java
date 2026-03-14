package com.company.vzvod.view.user;

import com.company.vzvod.entity.User;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.textfield.PasswordField;
import io.jmix.core.DataManager;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@ViewController("ChangePasswordView")
@ViewDescriptor("change-password-view.xml")
public class ChangePasswordView extends StandardView {

    private UUID userId;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Notifications notifications;

    @ViewComponent
    private PasswordField oldPasswordField;

    @ViewComponent
    private PasswordField newPasswordField;

    @ViewComponent
    private PasswordField confirmPasswordField;

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    @Subscribe("saveButton")
    public void onSaveButtonClick(ClickEvent<JmixButton> event) {

        if (userId == null) {
            notifications.create("Не определён пользователь").show();
            return;
        }

        String oldPass = oldPasswordField.getValue();
        String newPass = newPasswordField.getValue();
        String confirm = confirmPasswordField.getValue();

        if (newPass == null || newPass.isBlank()) {
            notifications.create("Введите новый пароль").show();
            return;
        }
        if (!newPass.equals(confirm)) {
            notifications.create("Новые пароли не совпадают").show();
            return;
        }

        User user = dataManager.load(User.class).id(userId).one();

        if (user.getPassword() != null) {
            if (oldPass == null || !passwordEncoder.matches(oldPass, user.getPassword())) {
                notifications.create("Старый пароль неверен").show();
                return;
            }
        }

        user.setPassword(passwordEncoder.encode(newPass));
        dataManager.save(user);

        close(StandardOutcome.SAVE);
    }

    @Subscribe("cancelButton")
    public void onCancelButtonClick(ClickEvent<JmixButton> event) {
        close(StandardOutcome.CLOSE);
    }
}