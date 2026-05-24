package com.company.vzvod.audit;

import com.company.vzvod.entity.Address;
import com.company.vzvod.entity.Contacts;
import com.company.vzvod.entity.Education;
import com.company.vzvod.entity.IdCard;
import com.company.vzvod.entity.Penalty;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import com.company.vzvod.entity.Vehicle;
import com.company.vzvod.entity.Vocation;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.event.EntityChangedEvent;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import jakarta.persistence.Entity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EmployeeAuditSupport {

    private final DataManager dataManager;
    private final Metadata metadata;
    private final Messages messages;
    private final MessageTools messageTools;
    private final MetadataTools metadataTools;

    public EmployeeAuditSupport(
            DataManager dataManager,
            Metadata metadata,
            Messages messages,
            MessageTools messageTools,
            MetadataTools metadataTools
    ) {
        this.dataManager = dataManager;
        this.metadata = metadata;
        this.messages = messages;
        this.messageTools = messageTools;
        this.metadataTools = metadataTools;
    }

    public boolean isAuditableEntity(Class<?> entityClass) {
        return entityClass != null
                && entityClass.getName().startsWith("com.company.vzvod.entity.")
                && entityClass.isAnnotationPresent(Entity.class);
    }

    public String entityCaption(Class<?> entityClass) {
        MetaClass metaClass = metadata.getClass(entityClass);
        if (metaClass == null) {
            return entityClass.getSimpleName();
        }
        try {
            return messages.getMessage(metaClass.getJavaClass(), metaClass.getName());
        } catch (Exception ignored) {
            return metaClass.getName();
        }
    }

    public String propertyCaption(Class<?> entityClass, String propertyName) {
        MetaClass metaClass = metadata.getClass(entityClass);
        if (metaClass == null) {
            return EmployeeAuditMessageFormatter.fieldLabel(propertyName);
        }
        MetaProperty property = metaClass.findProperty(propertyName);
        if (property == null) {
            return EmployeeAuditMessageFormatter.fieldLabel(propertyName);
        }
        try {
            return messageTools.getPropertyCaption(property);
        } catch (Exception ignored) {
            return EmployeeAuditMessageFormatter.fieldLabel(propertyName);
        }
    }

    public String instanceRef(Class<?> entityClass, Object entityId, Object loadedEntity) {
        if (loadedEntity != null) {
            String instanceName = metadataTools.getInstanceName(loadedEntity);
            if (instanceName != null && !instanceName.isBlank()) {
                return instanceName;
            }
        }
        if (entityId != null) {
            return entityId.toString();
        }
        return "—";
    }

    public Object loadEntity(EntityChangedEvent<?> event) {
        if (event.getType() == io.jmix.core.event.EntityChangedEvent.Type.DELETED) {
            return null;
        }
        return dataManager.load(event.getEntityId()).one();
    }

    public String resolveEmployeeFio(Object entity) {
        if (entity == null) {
            return "—";
        }
        if (entity instanceof User user) {
            return user.getShortFio();
        }
        User linked = findLinkedUser(entity);
        return linked != null ? linked.getShortFio() : "—";
    }

    public String resolveEmployeeFio(Class<?> entityClass, Object entityId) {
        if (entityClass == null || entityId == null) {
            return "—";
        }
        if (!(entityId instanceof UUID uuid)) {
            return "—";
        }
        if (User.class.equals(entityClass)) {
            User user = dataManager.load(User.class).id(uuid).optional().orElse(null);
            return user != null ? user.getShortFio() : "—";
        }
        Object entity = dataManager.load(entityClass).id(uuid).optional().orElse(null);
        if (entity != null) {
            return resolveEmployeeFio(entity);
        }
        return resolveEmployeeFioFromReferences(entityClass, entityId);
    }

    private String resolveEmployeeFioFromReferences(Class<?> entityClass, Object entityId) {
        if (!(entityId instanceof UUID uuid)) {
            return "—";
        }
        if (Contacts.class.equals(entityClass)) {
            Contacts c = dataManager.load(Contacts.class).id(uuid).optional().orElse(null);
            return c != null && c.getUser() != null ? c.getUser().getShortFio() : "—";
        }
        if (ServiceInfo.class.equals(entityClass)) {
            ServiceInfo s = dataManager.load(ServiceInfo.class).id(uuid).optional().orElse(null);
            return s != null && s.getUser() != null ? s.getUser().getShortFio() : "—";
        }
        if (Education.class.equals(entityClass)) {
            Education e = dataManager.load(Education.class).id(uuid).optional().orElse(null);
            User user = findUserByEducation(e);
            return user != null ? user.getShortFio() : "—";
        }
        if (Vehicle.class.equals(entityClass)) {
            Vehicle v = dataManager.load(Vehicle.class).id(uuid).optional().orElse(null);
            return v != null && v.getUser() != null ? v.getUser().getShortFio() : "—";
        }
        if (Vocation.class.equals(entityClass)) {
            Vocation v = dataManager.load(Vocation.class).id(uuid).optional().orElse(null);
            if (v != null && v.getUserServiceInfo() != null && v.getUserServiceInfo().getUser() != null) {
                return v.getUserServiceInfo().getUser().getShortFio();
            }
        }
        if (Penalty.class.equals(entityClass)) {
            Penalty p = dataManager.load(Penalty.class).id(uuid).optional().orElse(null);
            if (p != null && p.getUserServiceInfo() != null && p.getUserServiceInfo().getUser() != null) {
                return p.getUserServiceInfo().getUser().getShortFio();
            }
        }
        if (IdCard.class.equals(entityClass)) {
            IdCard idCard = dataManager.load(IdCard.class).id(uuid).optional().orElse(null);
            User user = findUserByIdCard(idCard);
            return user != null ? user.getShortFio() : "—";
        }
        if (Address.class.equals(entityClass)) {
            Address address = dataManager.load(Address.class).id(uuid).optional().orElse(null);
            User user = findUserByAddress(address);
            return user != null ? user.getShortFio() : "—";
        }
        return "—";
    }

    private User findLinkedUser(Object entity) {
        if (entity == null) {
            return null;
        }
        if (entity instanceof User user) {
            return user;
        }
        User viaUserProperty = readUserPropertyIfPresent(entity);
        if (viaUserProperty != null) {
            return viaUserProperty;
        }
        if (entity instanceof IdCard idCard) {
            return findUserByIdCard(idCard);
        }
        if (entity instanceof Address address) {
            return findUserByAddress(address);
        }
        if (entity instanceof Vocation vocation && vocation.getUserServiceInfo() != null) {
            return vocation.getUserServiceInfo().getUser();
        }
        if (entity instanceof Penalty penalty && penalty.getUserServiceInfo() != null) {
            return penalty.getUserServiceInfo().getUser();
        }
        if (entity instanceof Education education) {
            return findUserByEducation(education);
        }
        return null;
    }

    private User readUserPropertyIfPresent(Object entity) {
        MetaClass metaClass = metadata.getClass(entity.getClass());
        if (metaClass == null || metaClass.findProperty("user") == null) {
            return null;
        }
        Object userRef = EntityValues.getValue(entity, "user");
        return userRef instanceof User user ? user : null;
    }

    private User findUserByIdCard(IdCard idCard) {
        if (idCard == null || idCard.getId() == null) {
            return null;
        }
        return dataManager.load(User.class)
                .query("select u from User u where u.serviceInfo.idCard.id = :idCardId")
                .parameter("idCardId", idCard.getId())
                .optional()
                .orElse(null);
    }

    private User findUserByAddress(Address address) {
        if (address == null || address.getId() == null) {
            return null;
        }
        return dataManager.load(User.class)
                .query("select u from User u where u.contactsInfo.registration.id = :addressId"
                        + " or u.contactsInfo.habitation.id = :addressId")
                .parameter("addressId", address.getId())
                .optional()
                .orElse(null);
    }

    private User findUserByEducation(Education education) {
        if (education == null || education.getId() == null) {
            return null;
        }
        return dataManager.load(User.class)
                .query("select u from User u where u.education.id = :eduId")
                .parameter("eduId", education.getId())
                .optional()
                .orElse(null);
    }
}
