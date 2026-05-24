package com.company.vzvod.audit;

import com.vaadin.flow.component.UI;
import io.jmix.flowui.view.View;
import org.springframework.context.event.EventListener;

/**
 * Регистрация аудита кликов без Spring AOP на классах view — иначе CGLIB-прокси ломают закрытие диалогов.
 */
@org.springframework.stereotype.Component
public class ViewButtonAuditRegistrationListener {

    private final VaadinButtonClickAuditListener buttonClickAuditListener;

    public ViewButtonAuditRegistrationListener(VaadinButtonClickAuditListener buttonClickAuditListener) {
        this.buttonClickAuditListener = buttonClickAuditListener;
    }

    @EventListener
    public void onViewBeforeShow(View.BeforeShowEvent event) {
        View<?> view = event.getSource();
        if (!(view instanceof com.vaadin.flow.component.Component root)) {
            return;
        }
        if (!view.getClass().getName().startsWith("com.company.vzvod.view.")) {
            return;
        }
        UI ui = UI.getCurrent();
        if (ui == null || !ui.isAttached()) {
            return;
        }
        ui.access(() -> buttonClickAuditListener.registerButtonsIn(root));
    }
}
