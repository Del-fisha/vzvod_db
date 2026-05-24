package com.company.vzvod.audit;

import com.company.vzvod.entity.User;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.event.AttributeChanges;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class EmployeeAuditService {

    private static final Set<String> SKIPPED_ATTRIBUTES = Set.of(
            "password",
            "authorities",
            "id"
    );

    private final EmployeeAuditSupport auditSupport;

    public EmployeeAuditService(EmployeeAuditSupport auditSupport) {
        this.auditSupport = auditSupport;
    }

    public List<String> buildAuditMessages(EntityChangedEvent<?> event, String actor, String changeSource) {
        if (event.getEntityId() == null || actor == null || actor.isBlank()) {
            return List.of();
        }

        Class<?> entityClass = event.getEntityId().getEntityClass();
        if (!auditSupport.isAuditableEntity(entityClass)) {
            return List.of();
        }

        String employeeFio = auditSupport.resolveEmployeeFio(entityClass, event.getEntityId().getValue());

        List<String> messages = switch (event.getType()) {
            case DELETED -> List.of(buildDeletedMessage(actor, entityClass, employeeFio, event));
            case CREATED -> List.of(buildCreatedMessage(actor, entityClass, employeeFio, event));
            case UPDATED -> buildUpdatedMessages(event, actor, employeeFio);
        };

        return messages.stream()
                .map(message -> appendChangeSource(message, changeSource))
                .toList();
    }

    private String buildDeletedMessage(
            String actor,
            Class<?> entityClass,
            String employeeFio,
            EntityChangedEvent<?> event
    ) {
        if (EmployeeAuditMessageFormatter.isEmployeeRelatedEntity(entityClass)) {
            if (User.class.equals(entityClass)) {
                return EmployeeAuditMessageFormatter.deletedUserMessage(actor, employeeFio);
            }
            String entityLabel = EmployeeAuditMessageFormatter.entityTypeLabel(entityClass);
            return EmployeeAuditMessageFormatter.deletedEntityMessage(actor, entityLabel, employeeFio);
        }
        String caption = auditSupport.entityCaption(entityClass);
        String ref = auditSupport.instanceRef(entityClass, event.getEntityId().getValue(), null);
        return GenericEntityAuditMessageFormatter.deletedMessage(actor, caption, ref, employeeFio);
    }

    private String buildCreatedMessage(
            String actor,
            Class<?> entityClass,
            String employeeFio,
            EntityChangedEvent<?> event
    ) {
        Object entity = auditSupport.loadEntity(event);
        if (EmployeeAuditMessageFormatter.isEmployeeRelatedEntity(entityClass)) {
            if (User.class.equals(entityClass)) {
                return EmployeeAuditMessageFormatter.createdUserMessage(actor, employeeFio);
            }
            String entityLabel = EmployeeAuditMessageFormatter.entityTypeLabel(entityClass);
            return EmployeeAuditMessageFormatter.addedEntityMessage(actor, entityLabel, employeeFio);
        }
        String caption = auditSupport.entityCaption(entityClass);
        String ref = auditSupport.instanceRef(entityClass, event.getEntityId().getValue(), entity);
        return GenericEntityAuditMessageFormatter.createdMessage(actor, caption, ref, employeeFio);
    }

    private List<String> buildUpdatedMessages(EntityChangedEvent<?> event, String actor, String employeeFio) {
        AttributeChanges changes = event.getChanges();
        if (changes.getAttributes().isEmpty()) {
            return List.of();
        }

        Class<?> entityClass = event.getEntityId().getEntityClass();
        Object entity = auditSupport.loadEntity(event);
        if (entity == null) {
            return List.of();
        }

        boolean employeeRelated = EmployeeAuditMessageFormatter.isEmployeeRelatedEntity(entityClass);
        String entityCaption = auditSupport.entityCaption(entityClass);
        String instanceRef = auditSupport.instanceRef(entityClass, event.getEntityId().getValue(), entity);

        List<String> messages = new ArrayList<>();
        for (String property : changes.getAttributes()) {
            if (!changes.isChanged(property) || SKIPPED_ATTRIBUTES.contains(property)) {
                continue;
            }
            String fieldLabel = auditSupport.propertyCaption(entityClass, property);
            Object newValue = EntityValues.getValue(entity, property);
            if (employeeRelated) {
                if (newValue == null) {
                    messages.add(EmployeeAuditMessageFormatter.deletedFieldMessage(actor, fieldLabel, employeeFio));
                } else {
                    messages.add(EmployeeAuditMessageFormatter.changedMessage(actor, "изменил", fieldLabel, employeeFio));
                }
            } else if (newValue == null) {
                messages.add(GenericEntityAuditMessageFormatter.clearedFieldMessage(
                        actor, fieldLabel, entityCaption, instanceRef, employeeFio));
            } else {
                messages.add(GenericEntityAuditMessageFormatter.changedFieldMessage(
                        actor, fieldLabel, entityCaption, instanceRef, employeeFio));
            }
        }
        return messages;
    }

    private static String appendChangeSource(String message, String changeSource) {
        if (changeSource == null || changeSource.isBlank()) {
            return message;
        }
        return message + ". " + changeSource;
    }
}
