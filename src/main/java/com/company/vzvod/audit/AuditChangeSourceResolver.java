package com.company.vzvod.audit;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.stereotype.Component;

@Component
public class AuditChangeSourceResolver {

    private static final String MANUAL_UI = "Способ: вручную (интерфейс)";

    public String resolve() {
        String explicit = AuditChangeContext.getReason();
        if (explicit != null && !explicit.isBlank()) {
            return "Причина: " + explicit.trim();
        }
        if (isManualUiChange()) {
            return MANUAL_UI;
        }
        return "Способ: программно — " + detectProgrammaticCaller();
    }

    private boolean isManualUiChange() {
        try {
            return VaadinSession.getCurrent() != null && UI.getCurrent() != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    private String detectProgrammaticCaller() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            String className = element.getClassName();
            if (!className.startsWith("com.company.vzvod.")) {
                continue;
            }
            if (className.contains(".audit.")) {
                continue;
            }
            String simpleName = className.substring(className.lastIndexOf('.') + 1);
            return simpleName + "." + element.getMethodName();
        }
        return "фоновая задача";
    }
}
