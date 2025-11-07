package com.company.vzvod.view.user;

import com.company.vzvod.entity.User;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;

@Route(value = "users/:id", layout = MainViewTopMenu.class)
@ViewController(id = "User.detail")
@ViewDescriptor(path = "user-detail-view.xml")
@EditedEntityContainer("userDc")
public class UserDetailView extends StandardDetailView<User> {
    @Subscribe(id = "serviceInfoCreateButton", subject = "clickListener")
    public void onServiceInfoCreateButtonClick(final ClickEvent<JmixButton> event) {
        
    }
}