package com.company.vzvod.audit;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

@Component
public class ButtonClickAuditInitListener implements VaadinServiceInitListener {

    private final VaadinButtonClickAuditListener buttonClickAuditListener;

    public ButtonClickAuditInitListener(VaadinButtonClickAuditListener buttonClickAuditListener) {
        this.buttonClickAuditListener = buttonClickAuditListener;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiInit ->
                uiInit.getUI().access(() -> buttonClickAuditListener.registerButtonsIn(uiInit.getUI())));
    }
}
