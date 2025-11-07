package com.company.vzvod.view.user;

import com.company.vzvod.entity.User;

import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "main_users/:id", layout = MainViewTopMenu.class)
@ViewController(id = "Main.user.detail")
@ViewDescriptor(path = "main-user-detail-view.xml")
@EditedEntityContainer("userDc")
public class MainUserDetailView extends StandardDetailView<User> {
}