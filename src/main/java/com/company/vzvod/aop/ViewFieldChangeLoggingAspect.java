package com.company.vzvod.aop;

import com.company.vzvod.entity.User;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.model.InstanceContainer;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
@Component
public class ViewFieldChangeLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ViewFieldChangeLoggingAspect.class);

    private final CurrentAuthentication currentAuthentication;

    public ViewFieldChangeLoggingAspect(CurrentAuthentication currentAuthentication) {
        this.currentAuthentication = currentAuthentication;
    }

    @Pointcut("execution(* com.company.vzvod.view..*.on*ItemPropertyChange(..)) && args(event)")
    public void anyItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<?> event) {
    }

    @After("anyItemPropertyChange(event)")
    public void logFieldChange(InstanceContainer.ItemPropertyChangeEvent<?> event) {
        Object principal = currentAuthentication.getUser();
        String username = principal instanceof User u ? u.getUsername() : String.valueOf(principal);

        String entityName = event.getItem().getClass().getSimpleName();
        String property = event.getProperty();
        Object oldVal = event.getPrevValue();
        Object newVal = event.getValue();

        log.info("""
                Пользователь '{}' изменил поле '{}' сущности '{}'.
                Предыдущее значение: {}
                Новое значение:      {}
                
                """,
                username,
                property,
                entityName,
                oldVal,
                newVal
        );
    }
}