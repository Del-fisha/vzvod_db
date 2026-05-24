package com.company.vzvod.logging;

import com.vaadin.flow.server.DefaultErrorHandler;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ErrorHandler;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.SessionInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VaadinUiErrorLoggingHandler implements VaadinServiceInitListener {

    private static final Logger log = LoggerFactory.getLogger(VaadinUiErrorLoggingHandler.class);

    private final LoggingServiceClient loggingClient;

    public VaadinUiErrorLoggingHandler(LoggingServiceClient loggingClient) {
        this.loggingClient = loggingClient;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addSessionInitListener(this::wrapSessionErrorHandler);
        event.getSource().addUIInitListener(uiInit -> {
            wrapSessionErrorHandler(uiInit.getUI().getSession());
            uiInit.getUI().addAfterNavigationListener(nav ->
                    wrapSessionErrorHandler(uiInit.getUI().getSession()));
        });
    }

    private void wrapSessionErrorHandler(SessionInitEvent sessionInitEvent) {
        wrapSessionErrorHandler(sessionInitEvent.getSession());
    }

    private void wrapSessionErrorHandler(VaadinSession session) {
        if (session == null) {
            return;
        }
        ErrorHandler current = session.getErrorHandler();
        if (current instanceof LoggingErrorHandlerWrapper) {
            return;
        }
        session.setErrorHandler(new LoggingErrorHandlerWrapper(loggingClient, current));
    }

    private void report(Throwable throwable) {
        if (throwable == null) {
            return;
        }
        log.error("Ошибка UI Core", throwable);
        loggingClient.logError(VaadinUiErrorLoggingHandler.formatUiError(throwable));
    }

    static String formatUiError(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            message = throwable.getClass().getSimpleName();
        }
        StringBuilder sb = new StringBuilder("Ошибка UI: ").append(message);
        for (Throwable cause = throwable.getCause(); cause != null; cause = cause.getCause()) {
            if (cause.getMessage() != null && !cause.getMessage().isBlank()) {
                sb.append(" | причина: ").append(cause.getMessage());
            }
        }
        StackTraceElement[] stack = throwable.getStackTrace();
        int limit = Math.min(stack.length, 8);
        for (int i = 0; i < limit; i++) {
            sb.append("\n  at ").append(stack[i]);
        }
        String result = sb.toString();
        return result.length() > 4000 ? result.substring(0, 4000) : result;
    }

    static final class LoggingErrorHandlerWrapper implements ErrorHandler {

        private final LoggingServiceClient loggingClient;
        private final ErrorHandler delegate;

        LoggingErrorHandlerWrapper(LoggingServiceClient loggingClient, ErrorHandler delegate) {
            this.loggingClient = loggingClient;
            this.delegate = delegate;
        }

        @Override
        public void error(ErrorEvent event) {
            Throwable throwable = event.getThrowable();
            if (throwable != null) {
                log.error("Ошибка UI Core (ErrorHandler)", throwable);
                loggingClient.logError(formatUiError(throwable));
            }
            if (delegate != null) {
                delegate.error(event);
            } else {
                new DefaultErrorHandler().error(event);
            }
        }
    }
}
