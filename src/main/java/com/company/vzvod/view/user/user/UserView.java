package com.company.vzvod.view.user.user;


import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "user-view", layout = MainViewTopMenu.class)
@ViewController(id = "UserView")
@ViewDescriptor(path = "user-view.xml")
public class UserView extends StandardView {
}