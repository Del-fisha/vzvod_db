package com.company.vzvod.notification;

import com.company.vzvod.entity.Post;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import com.company.vzvod.entity.UserNotification;
import com.company.vzvod.entity.UserNotificationRecipient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.core.Metadata;
import io.jmix.core.UnconstrainedDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;

@Service
public class UserNotificationService {

    private static final Logger log = LoggerFactory.getLogger(UserNotificationService.class);

    private final UnconstrainedDataManager dataManager;
    private final Metadata metadata;
    private final ObjectMapper objectMapper;

    public UserNotificationService(UnconstrainedDataManager dataManager, Metadata metadata, ObjectMapper objectMapper) {
        this.dataManager = dataManager;
        this.metadata = metadata;
        this.objectMapper = objectMapper;
    }

    public UUID createOverdueNotification(OverdueNotificationRequest request, User createdBy) {
        if (request == null || request.userId() == null || request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Empty request");
        }

        log.info("Creating OVERDUE notification: subjectUserId={}, items={}", request.userId(), request.items());

        User subjectUser = dataManager.load(User.class).id(request.userId()).one();

        ServiceInfo subjectServiceInfo = dataManager.load(ServiceInfo.class)
                .query("select si from ServiceInfo si left join fetch si.department where si.user.id = :uid")
                .parameter("uid", subjectUser.getId())
                .optional()
                .orElse(null);

        Set<UUID> recipientUserIds = new LinkedHashSet<>();
        recipientUserIds.add(subjectUser.getId());

        UUID departmentId = null;
        if (subjectServiceInfo != null && subjectServiceInfo.getDepartment() != null) {
            departmentId = subjectServiceInfo.getDepartment().getId();
        }

        if (departmentId != null) {
            List<UUID> comOtdUserIds = dataManager.loadValue(
                            "select u.id from User u join u.serviceInfo si where si.department.id = :depId and si.post = :post",
                            UUID.class)
                    .parameter("depId", departmentId)
                    .parameter("post", Post.COM_OTD.getId())
                    .list();
            recipientUserIds.addAll(comOtdUserIds);
        }

        log.info("Recipients resolved: subjectUserId={}, departmentId={}, recipients={}",
                subjectUser.getId(), departmentId, recipientUserIds);

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(new StoredOverduePayload(subjectUser.getId(), request.items()));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize payload", e);
        }

        UserNotification n = metadata.create(UserNotification.class);
        n.setKind(UserNotificationKind.OVERDUE);
        n.setPayload(payloadJson);
        n.setCreatedAt(OffsetDateTime.now());
        n.setCreatedByUser(createdBy);

        dataManager.save(n);
        log.info("Notification saved: id={}, kind={}, createdAt={}", n.getId(), n.getKind(), n.getCreatedAt());

        for (UUID uid : recipientUserIds) {
            User u = dataManager.load(User.class).id(uid).one();
            UserNotificationRecipient r = metadata.create(UserNotificationRecipient.class);
            r.setNotification(n);
            r.setUser(u);
            dataManager.save(r);
        }

        return n.getId();
    }

    public List<UserNotification> loadActiveForUser(UUID userId) {
        List<UserNotification> list = dataManager.load(UserNotification.class)
                .query("""
                        select n from UserNotification n
                        join UserNotificationRecipient r on r.notification.id = n.id
                        where r.user.id = :uid
                          and n.resolvedAt is null
                        order by n.createdAt desc
                        """)
                .parameter("uid", userId)
                .list();
        log.info("Loaded active notifications for user {}: count={}", userId, list.size());
        return list;
    }

    public void resolve(UUID notificationId, UUID resolverUserId) {
        UserNotification n = dataManager.load(UserNotification.class).id(notificationId).one();
        if (n.getResolvedAt() != null) {
            log.info("Resolve skipped (already resolved): notificationId={}, resolvedAt={}", notificationId, n.getResolvedAt());
            return;
        }
        User resolver = dataManager.load(User.class).id(resolverUserId).one();
        n.setResolvedAt(OffsetDateTime.now());
        n.setResolvedByUser(resolver);
        dataManager.save(n);
        log.info("Resolved notification: id={}, resolverUserId={}", notificationId, resolverUserId);
    }

    public record StoredOverduePayload(UUID subjectUserId, List<OverdueItemDto> items) {
    }
}

