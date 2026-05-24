package com.company.vzvod.audit;

public final class UiClickAuditMessages {

    private UiClickAuditMessages() {
    }

    public static String buttonClick(String actorFio, String buttonLabel, String pageContext) {
        String actor = actorFio == null || actorFio.isBlank() ? "Неизвестный пользователь" : actorFio.trim();
        String label = buttonLabel == null || buttonLabel.isBlank() ? "без подписи" : buttonLabel.trim();
        if (pageContext == null || pageContext.isBlank()) {
            return String.format("%s нажал кнопку \"%s\"", actor, label);
        }
        return String.format("%s нажал кнопку \"%s\" %s", actor, label, pageContext.trim());
    }
}
