package com.company.vzvod.audit;

import com.company.vzvod.entity.User;
import com.company.vzvod.view.user.UserDetailView;
import com.company.vzvod.view.usercard.UserCardView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.QueryParameters;
import io.jmix.core.DataManager;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@org.springframework.stereotype.Component
public class UiClickPageContextResolver {

    private static final Pattern UUID_IN_PATH = Pattern.compile(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
    );

    private final DataManager dataManager;

    public UiClickPageContextResolver(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public String resolve(com.vaadin.flow.component.Component clickSource) {
        String fromView = resolveFromViewHierarchy(clickSource);
        if (fromView != null && !fromView.isBlank()) {
            return fromView;
        }
        UI ui = UI.getCurrent();
        if (ui == null) {
            return "";
        }
        Location location = ui.getInternals().getActiveViewLocation();
        if (location == null) {
            return "";
        }
        return resolveFromLocation(location.getPath(), location.getQueryParameters());
    }

    private String resolveFromViewHierarchy(com.vaadin.flow.component.Component component) {
        com.vaadin.flow.component.Component current = component;
        while (current != null) {
            if (current instanceof UserDetailView userDetailView) {
                return employeeContext(userDetailView.getViewedUser());
            }
            if (current instanceof UserCardView userCardView) {
                return employeeContext(userCardView.getViewedUser());
            }
            current = current.getParent().orElse(null);
        }
        return "";
    }

    String resolveFromLocation(String path, QueryParameters queryParameters) {
        if (path == null || path.isBlank()) {
            return "";
        }
        String normalized = path.startsWith("/") ? path.substring(1) : path;

        if ("users".equals(normalized)) {
            return "на странице списка сотрудников";
        }

        if (normalized.startsWith("users/")) {
            String idSegment = normalized.substring("users/".length());
            int slash = idSegment.indexOf('/');
            if (slash >= 0) {
                idSegment = idSegment.substring(0, slash);
            }
            return employeeContext(loadUserFio(idSegment));
        }

        if (normalized.startsWith("user-card")) {
            String userId = firstQueryParam(queryParameters, "userId");
            if (userId != null) {
                return employeeContext(loadUserFio(userId));
            }
            return "на странице карточки сотрудника";
        }

        var uuidMatcher = UUID_IN_PATH.matcher(normalized);
        if (uuidMatcher.find()) {
            return employeeContext(loadUserFio(uuidMatcher.group()));
        }

        return "на странице " + humanizePath(normalized);
    }

    private static String firstQueryParam(QueryParameters queryParameters, String name) {
        if (queryParameters == null) {
            return null;
        }
        List<String> values = queryParameters.getParameters().get(name);
        if (values == null || values.isEmpty() || values.getFirst() == null || values.getFirst().isBlank()) {
            return null;
        }
        return values.getFirst().trim();
    }

    private String loadUserFio(String idText) {
        if (idText == null || idText.isBlank()) {
            return null;
        }
        try {
            UUID id = UUID.fromString(idText.trim());
            return dataManager.load(User.class).id(id).optional()
                    .map(User::getShortFio)
                    .orElse(null);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String employeeContext(User user) {
        if (user == null) {
            return "";
        }
        String fio = user.getShortFio();
        if (fio == null || fio.isBlank()) {
            return "";
        }
        return "у сотрудника " + fio.trim();
    }

    private static String employeeContext(String fio) {
        if (fio == null || fio.isBlank()) {
            return "";
        }
        return "у сотрудника " + fio.trim();
    }

    private static String humanizePath(String path) {
        int slash = path.indexOf('/');
        if (slash < 0) {
            return path;
        }
        return path.substring(0, slash);
    }
}
