package com.company.vzvod.audit;

import com.company.vzvod.entity.User;
import io.jmix.core.DataManager;
import io.jmix.core.security.CurrentAuthentication;
import org.springframework.stereotype.Component;

@Component
public class AuditActorResolver {

    private final CurrentAuthentication currentAuthentication;
    private final DataManager dataManager;

    public AuditActorResolver(CurrentAuthentication currentAuthentication, DataManager dataManager) {
        this.currentAuthentication = currentAuthentication;
        this.dataManager = dataManager;
    }

    public String resolveActorFio() {
        if (currentAuthentication.getAuthentication() == null) {
            return null;
        }
        Object principal = currentAuthentication.getUser();
        if (principal instanceof User user) {
            return user.getShortFio();
        }
        return null;
    }

    /**
     * ФИО из сессии, иначе логин, иначе «Система» — чтобы аудит не терялся без Vaadin-пользователя.
     */
    public String resolveActorLabel() {
        String fio = resolveActorFio();
        if (fio != null && !fio.isBlank()) {
            return fio;
        }
        if (currentAuthentication.getAuthentication() == null) {
            return "Система";
        }
        String username = currentAuthentication.getAuthentication().getName();
        if (username == null || username.isBlank()) {
            return "Система";
        }
        return dataManager.load(User.class)
                .query("select u from User u where u.username = :username")
                .parameter("username", username)
                .optional()
                .map(User::getShortFio)
                .orElse(username);
    }
}
