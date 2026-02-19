package com.company.vzvod.aop;

import com.company.vzvod.entity.User;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.view.StandardView;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        String username = principal instanceof User u ? u.getLastName() + " " + u.getLastName() : String.valueOf(principal);

        StandardView view = (StandardView) jp.getTarget();
        String viewId = view.getId().orElse(view.getClass().getSimpleName());

        log.info("""
                Пользователь '{}' открыл страницу '{}'.
                
                """, username, viewId);
    }
    // ToDo Добавить "Чью страницу" открыл
}