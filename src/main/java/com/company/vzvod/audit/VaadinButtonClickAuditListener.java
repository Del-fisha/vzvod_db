package com.company.vzvod.audit;

import com.company.vzvod.logging.LoggingServiceClient;
import com.vaadin.flow.component.button.Button;

import java.util.concurrent.CompletableFuture;

@org.springframework.stereotype.Component
public class VaadinButtonClickAuditListener {

    private static final String AUDIT_REGISTERED = "data-audit-click-registered";
    private static final String AUDIT_ATTACH_WATCHER = "data-audit-attach-watcher";

    private final LoggingServiceClient loggingClient;
    private final AuditActorResolver actorResolver;
    private final UiClickPageContextResolver pageContextResolver;

    public VaadinButtonClickAuditListener(
            LoggingServiceClient loggingClient,
            AuditActorResolver actorResolver,
            UiClickPageContextResolver pageContextResolver
    ) {
        this.loggingClient = loggingClient;
        this.actorResolver = actorResolver;
        this.pageContextResolver = pageContextResolver;
    }

    public void registerButtonsIn(com.vaadin.flow.component.Component root) {
        registerAttachWatcherIfNeeded(root);
        if (root instanceof Button button) {
            registerIfNeeded(button);
        }
        root.getChildren().forEach(this::registerButtonsIn);
    }

    private void registerAttachWatcherIfNeeded(com.vaadin.flow.component.Component component) {
        if ("true".equals(component.getElement().getAttribute(AUDIT_ATTACH_WATCHER))) {
            return;
        }
        component.getElement().setAttribute(AUDIT_ATTACH_WATCHER, "true");
        component.addAttachListener(event -> registerButtonsIn(event.getSource()));
    }

    public void registerIfNeeded(Button button) {
        if ("true".equals(button.getElement().getAttribute(AUDIT_REGISTERED))) {
            return;
        }
        button.getElement().setAttribute(AUDIT_REGISTERED, "true");
        button.addClickListener(e -> onButtonClick(button));
    }

    private void onButtonClick(Button button) {
        String message = UiClickAuditMessages.buttonClick(
                actorResolver.resolveActorFio(),
                resolveButtonLabel(button),
                pageContextResolver.resolve(button)
        );
        // Не блокировать обработчики «Сохранить» / «Закрыть» на том же клике.
        CompletableFuture.runAsync(() -> loggingClient.logMain(message));
    }

    static String resolveButtonLabel(Button button) {
        String text = button.getText();
        if (text != null && !text.isBlank()) {
            return text;
        }
        String title = button.getElement().getAttribute("title");
        if (title != null && !title.isBlank()) {
            return title;
        }
        return button.getId().orElse(button.getClass().getSimpleName());
    }
}
