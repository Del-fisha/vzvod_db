package com.company.vzvod.aop;

import com.company.vzvod.entity.User;
import com.company.vzvod.view.user.UserDetailView;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ViewSaveLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ViewSaveLoggingAspect.class);

    @AfterReturning(
            value = "execution(* com.company.vzvod.view..*DetailView.on*Save*(..)) && target(view)",
            returning = "result")
    public void logSavedFields(Object view, Object result) {

        if (view instanceof UserDetailView userView) {
            User user = userView.getEditedUser();

            log.info("""
                    Сохранение сущности User.
                    username = {}
                    firstName = {}
                    lastName = {}
                    patronymic = {}
                    dateOfBirth = {}
                    
                    """,
                    user.getUsername(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getPatronymic(),
                    user.getDateOfBirth()
            );
        }
    }
}