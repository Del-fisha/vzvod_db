package com.company.vzvod.view.user;

import com.company.vzvod.entity.User;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import io.jmix.flowui.view.*;

@AnonymousAllowed
@DialogMode(width = "64em", resizable = true, modal = true)
@Route(value = "registeruser", layout = MainViewTopMenu.class) // можно оставить, на диалог не влияет
@ViewController("User.detail.register")
@ViewDescriptor("register-user-detail-view.xml")
@EditedEntityContainer("userDc")
public class RegisterUserDetailView extends StandardDetailView<User> {
}