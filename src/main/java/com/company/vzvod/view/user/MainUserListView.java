package com.company.vzvod.view.user;

import com.company.vzvod.entity.User;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "main_users", layout = MainViewTopMenu.class)
@ViewController(id = "Main.user.list")
@ViewDescriptor(path = "main-user-list-view.xml")
@LookupComponent("usersDataGrid")
@DialogMode(width = "64em")
public class MainUserListView extends StandardListView<User> {

}