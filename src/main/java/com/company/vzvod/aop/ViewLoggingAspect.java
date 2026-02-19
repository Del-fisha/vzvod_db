package com.company.vzvod.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ViewLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ViewLoggingAspect.class);

    @Before("execution(public * com.company.vzvod.view..*(..))")
    public void logViewCall(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        log.info("Вызов представления: {}. Параметры: {}\n", methodName, args);
    }
}