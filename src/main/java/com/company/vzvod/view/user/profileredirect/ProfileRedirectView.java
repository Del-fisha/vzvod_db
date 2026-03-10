package com.company.vzvod.view.user.profileredirect;


import com.company.vzvod.entity.User;
import com.company.vzvod.view.mainviewtopmenu.MainViewTopMenu;
import com.company.vzvod.view.user.UserDetailView;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "profile", layout = MainViewTopMenu.class)
@ViewController("ProfileRedirect")
@ViewDescriptor("profile-redirect.xml")
public class ProfileRedirectView extends StandardView {

    @Autowired
    private CurrentAuthentication currentAuthentication;

    @Subscribe
    public void onReady(ReadyEvent event) {
        User user = (User) currentAuthentication.getUser();

        event.getSource().getUI().ifPresent(ui ->
                ui.navigate(UserDetailView.class,
                        new RouteParameters("id", user.getId().toString()))
        );
    }
}