package com.company.vzvod.audit;

import com.company.vzvod.entity.Contacts;
import com.company.vzvod.entity.Education;
import com.company.vzvod.entity.Penalty;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import com.company.vzvod.entity.Vehicle;
import com.company.vzvod.entity.Vocation;

import java.util.Map;
import java.util.Set;

final class EmployeeAuditMessageFormatter {

    static final Set<Class<?>> EMPLOYEE_RELATED_TYPES = Set.of(
            User.class,
            ServiceInfo.class,
            Contacts.class,
            Education.class,
            Vehicle.class,
            Vocation.class,
            Penalty.class
    );

    static boolean isEmployeeRelatedEntity(Class<?> entityClass) {
        return EMPLOYEE_RELATED_TYPES.contains(entityClass);
    }

    private static final Map<String, String> FIELD_LABELS = Map.ofEntries(
            Map.entry("firstName", "имя"),
            Map.entry("lastName", "фамилия"),
            Map.entry("patronymic", "отчество"),
            Map.entry("dateOfBirth", "дата рождения"),
            Map.entry("gender", "пол"),
            Map.entry("armyService", "служба"),
            Map.entry("status", "статус"),
            Map.entry("rank", "звание"),
            Map.entry("post", "должность"),
            Map.entry("department", "подразделение"),
            Map.entry("phone", "телефон"),
            Map.entry("email", "email"),
            Map.entry("breastplate", "номер жетона")
    );

    private EmployeeAuditMessageFormatter() {
    }

    static String fieldLabel(String property) {
        return FIELD_LABELS.getOrDefault(property, property);
    }

    static String entityTypeLabel(Class<?> entityClass) {
        if (User.class.equals(entityClass)) {
            return "сотрудника";
        }
        if (Education.class.equals(entityClass)) {
            return "образование";
        }
        if (Contacts.class.equals(entityClass)) {
            return "контакты";
        }
        if (ServiceInfo.class.equals(entityClass)) {
            return "служебные данные";
        }
        if (Vehicle.class.equals(entityClass)) {
            return "транспорт";
        }
        if (Vocation.class.equals(entityClass)) {
            return "отпуск";
        }
        if (Penalty.class.equals(entityClass)) {
            return "взыскание";
        }
        return entityClass.getSimpleName();
    }

    static String changedMessage(String actorFio, String action, String fieldLabel, String employeeFio) {
        return "%s %s \"%s\" у сотрудника %s".formatted(actorFio, action, fieldLabel, employeeFio);
    }

    static String deletedEntityMessage(String actorFio, String entityLabel, String employeeFio) {
        return "%s удалил %s у сотрудника %s".formatted(actorFio, entityLabel, employeeFio);
    }

    static String deletedFieldMessage(String actorFio, String fieldLabel, String employeeFio) {
        return "Удалено поле \"%s\" у сотрудника %s (%s)".formatted(fieldLabel, employeeFio, actorFio);
    }

    static String createdUserMessage(String actorFio, String employeeFio) {
        return "%s создал сотрудника %s".formatted(actorFio, employeeFio);
    }

    static String addedEntityMessage(String actorFio, String entityLabel, String employeeFio) {
        return "%s добавил %s у сотрудника %s".formatted(actorFio, entityLabel, employeeFio);
    }

    static String deletedUserMessage(String actorFio, String employeeFio) {
        return "%s удалил сотрудника %s".formatted(actorFio, employeeFio);
    }
}
