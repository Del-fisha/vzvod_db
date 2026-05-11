package com.company.vzvod.security.crypto;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Minimal Spring context holder for JPA converters (managed outside Spring).
 */
@Component
public class SpringContext implements ApplicationContextAware {

    private static volatile ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringContext.context = applicationContext;
    }

    public static <T> T getBean(Class<T> type) {
        ApplicationContext ctx = context;
        if (ctx == null) {
            throw new IllegalStateException("Spring ApplicationContext is not initialized yet");
        }
        return ctx.getBean(type);
    }
}

