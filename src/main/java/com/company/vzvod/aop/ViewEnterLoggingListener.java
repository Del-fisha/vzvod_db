package com.company.vzvod.aop;

import com.company.vzvod.entity.User;
import com.company.vzvod.view.user.UserDetailView;
import com.company.vzvod.view.usercard.UserCardView;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Profile("dev")
@Component
public class ViewEnterLoggingListener {

    private static final Logger log = LoggerFactory.getLogger(ViewEnterLoggingListener.class);

    private final CurrentAuthentication currentAuthentication;

    public ViewEnterLoggingListener(CurrentAuthentication currentAuthentication) {
        this.currentAuthentication = currentAuthentication;
    }

    @EventListener
    public void onViewBeforeShow(View.BeforeShowEvent event) {
        View<?> view = event.getSource();
        if (!(view instanceof StandardView standardView)) {
            return;
        }
        if (!view.getClass().getName().startsWith("com.company.vzvod.view.")) {
            return;
        }

        Object principal = currentAuthentication.getUser();
        String actor = principal instanceof User u
                ? u.getLastName() + " " + u.getFirstName()
                : String.valueOf(principal);

        String details = "";
        User viewed = null;
        if (view instanceof UserDetailView userDetailView) {
            viewed = userDetailView.getViewedUser();
        } else if (view instanceof UserCardView userCardView) {
            viewed = userCardView.getViewedUser();
        }
        if (viewed != null) {
            details = String.format(
                    " карточку пользователя %s %s",
                    viewed.getLastName(),
                    viewed.getFirstName()
            );
        }

        String viewId = standardView.getId().orElse(view.getClass().getSimpleName());
        log.info("Пользователь '{}' открыл страницу '{}'{}.", actor, viewId, details);
    }
}
