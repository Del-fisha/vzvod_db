package com.company.vzvod.aop;

import com.company.vzvod.entity.User;
import com.company.vzvod.view.user.UserDetailView;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.view.StandardView;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Profile("dev") // ToDo Разобраться с логированием
@Aspect
@Component
public class ViewEnterLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ViewEnterLoggingAspect.class);

    private final CurrentAuthentication currentAuthentication;

    public ViewEnterLoggingAspect(CurrentAuthentication currentAuthentication) {
        this.currentAuthentication = currentAuthentication;
    }

    @After("execution(* com.company.vzvod.view..*.onBeforeShow(..))")
    public void logViewEnter(JoinPoint jp) {
        Object principal = currentAuthentication.getUser();
        String actor = principal instanceof User u
                ? u.getLastName() + " " + u.getFirstName()
                : String.valueOf(principal);

        Object target = jp.getTarget();

        String details = "";
        if (target instanceof UserDetailView userDetailView) {
            User viewed = userDetailView.getViewedUser();
            if (viewed != null) {
                details = String.format(
                        " карточку пользователя %s %s",
                        viewed.getLastName(),
                        viewed.getFirstName()
                );
            }
        }

        StandardView view = (StandardView) target;
        String viewId = view.getId().orElse(view.getClass().getSimpleName());

        String message = String.format(
                "Пользователь '%s' открыл страницу '%s'%s.",
                actor,
                viewId,
                details
        );

        log.info(message);
    }
}
