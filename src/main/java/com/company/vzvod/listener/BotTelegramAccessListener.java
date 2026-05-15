package com.company.vzvod.listener;

import com.company.vzvod.bot.BotTelegramAccessMessages;
import com.company.vzvod.bot.BotTelegramAccessService;
import com.company.vzvod.entity.Contacts;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.StatusInService;
import com.company.vzvod.entity.User;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class BotTelegramAccessListener {

    private final BotTelegramAccessService accessService;
    private final DataManager dataManager;

    public BotTelegramAccessListener(BotTelegramAccessService accessService, DataManager dataManager) {
        this.accessService = accessService;
        this.dataManager = dataManager;
    }

    @EventListener
    public void onContactsChanged(EntityChangedEvent<Contacts> event) {
        if (event.getEntityId() == null) {
            return;
        }
        UUID contactsId = (UUID) event.getEntityId().getValue();
        if (event.getType() == EntityChangedEvent.Type.DELETED) {
            UUID userId = resolveUserIdFromContactsEvent(event, contactsId);
            if (userId != null) {
                accessService.closeAccess(userId, BotTelegramAccessMessages.PHONE_MISSING);
            }
            return;
        }
        if (event.getType() != EntityChangedEvent.Type.UPDATED) {
            return;
        }
        if (!event.getChanges().isChanged("phoneNumber")) {
            return;
        }
        Contacts contacts = dataManager.load(Contacts.class).id(contactsId).optional().orElse(null);
        if (contacts == null || contacts.getUser() == null) {
            return;
        }
        String phone = contacts.getPhoneNumber();
        if (phone == null || phone.isBlank()) {
            accessService.closeAccess(contacts.getUser().getId(), BotTelegramAccessMessages.PHONE_MISSING);
        } else {
            accessService.closeAccess(contacts.getUser().getId(), BotTelegramAccessMessages.PHONE_CHANGED);
        }
    }

    @EventListener
    public void onUserChanged(EntityChangedEvent<User> event) {
        if (event.getType() != EntityChangedEvent.Type.DELETED || event.getEntityId() == null) {
            return;
        }
        UUID userId = (UUID) event.getEntityId().getValue();
        accessService.closeAccess(userId, BotTelegramAccessMessages.USER_REMOVED);
    }

    @EventListener
    public void onServiceInfoChanged(EntityChangedEvent<ServiceInfo> event) {
        if (event.getEntityId() == null) {
            return;
        }
        if (event.getType() == EntityChangedEvent.Type.DELETED) {
            UUID userId = resolveUserIdFromServiceInfoEvent(event);
            if (userId != null) {
                accessService.closeAccess(userId, BotTelegramAccessMessages.NOT_ACTIVE);
            }
            return;
        }
        if (event.getType() != EntityChangedEvent.Type.UPDATED || !event.getChanges().isChanged("status")) {
            return;
        }
        ServiceInfo serviceInfo = dataManager.load(event.getEntityId()).one();
        if (serviceInfo.getUser() == null) {
            return;
        }
        if (serviceInfo.getStatus() != StatusInService.ACTIVE) {
            accessService.closeAccess(serviceInfo.getUser().getId(), BotTelegramAccessMessages.NOT_ACTIVE);
        }
    }

    private UUID resolveUserIdFromContactsEvent(EntityChangedEvent<Contacts> event, UUID contactsId) {
        Id<User> userRef = event.getChanges().getOldReferenceId("user");
        if (userRef != null) {
            Object raw = userRef.getValue();
            if (raw instanceof UUID u) {
                return u;
            }
        }
        Contacts contacts = dataManager.load(Contacts.class).id(contactsId).optional().orElse(null);
        return contacts != null && contacts.getUser() != null ? contacts.getUser().getId() : null;
    }

    private UUID resolveUserIdFromServiceInfoEvent(EntityChangedEvent<ServiceInfo> event) {
        Id<User> userRef = event.getChanges().getOldReferenceId("user");
        if (userRef == null) {
            return null;
        }
        Object raw = userRef.getValue();
        return raw instanceof UUID u ? u : null;
    }
}
