package com.company.vzvod.aop;

import com.company.vzvod.entity.User;
import com.company.vzvod.view.user.UserDetailView;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class UserDetailLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(UserDetailLoggingAspect.class);

    @Before("execution(* com.company.vzvod.view.user.UserDetailView.on*ButtonClick(..)) && target(view)")
    public void logBeforeButtonClick(JoinPoint joinPoint, UserDetailView view) {
        String methodName = joinPoint.getSignature().getName();

        User user = view.getEditedEntityOrNull();
        String name = user != null && user.getDisplayName() != null ? user.getDisplayName() : "null";

        log.info("Кнопка нажата: метод= {}, user= {}", methodName, name);
    }
}
