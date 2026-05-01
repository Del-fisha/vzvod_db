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

        log.info("Creating/Updating OVERDUE notification: subjectUserId={}, items={}", request.userId(), request.items());

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

        OffsetDateTime now = OffsetDateTime.now();
        String incomingFingerprint = fingerprint(subjectUser.getId(), request.items());

        // If user marked the issue as resolved ("Исправлено"), do not re-notify
        // until the actual overdue data changes (fingerprint changes).
        UserNotification latestForSubject = findLatestOverdueForSubject(subjectUser.getId());
        if (latestForSubject != null
                && latestForSubject.getResolvedAt() != null
                && incomingFingerprint.equals(fingerprintFromStoredPayload(latestForSubject))) {
            log.info("OVERDUE suppressed (already resolved, unchanged): subjectUserId={}, latestNotificationId={}",
                    subjectUser.getId(), latestForSubject.getId());
            return latestForSubject.getId();
        }

        // Make OVERDUE notifications unique per subjectUserId: update existing active one instead of creating duplicates.
        List<UserNotification> existingForSubject = loadActiveForUser(subjectUser.getId()).stream()
                .filter(n -> UserNotificationKind.OVERDUE.equals(n.getKind()))
                .filter(n -> subjectUser.getId().equals(tryExtractSubjectUserId(n)))
                .toList();

        UserNotification n;
        if (!existingForSubject.isEmpty()) {
            // list is already ordered by createdAt desc in loadActiveForUser()
            n = existingForSubject.getFirst();
            n.setPayload(payloadJson);
            n.setCreatedAt(now);
            n.setCreatedByUser(createdBy);
            dataManager.save(n);
            log.info("Notification updated: id={}, kind={}, createdAt={}", n.getId(), n.getKind(), n.getCreatedAt());

            // Close duplicates if any (to keep UI clean and semantics "unique problem").
            if (existingForSubject.size() > 1) {
                for (int i = 1; i < existingForSubject.size(); i++) {
                    UserNotification dup = existingForSubject.get(i);
                    dup.setResolvedAt(now);
                    dup.setResolvedByUser(createdBy);
                    dataManager.save(dup);
                    log.info("Duplicate OVERDUE notification auto-resolved: id={}", dup.getId());
                }
            }
        } else {
            n = metadata.create(UserNotification.class);
            n.setKind(UserNotificationKind.OVERDUE);
            n.setPayload(payloadJson);
            n.setCreatedAt(now);
            n.setCreatedByUser(createdBy);
            dataManager.save(n);
            log.info("Notification saved: id={}, kind={}, createdAt={}", n.getId(), n.getKind(), n.getCreatedAt());
        }

        syncRecipients(n, recipientUserIds);

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

    private UUID tryExtractSubjectUserId(UserNotification n) {
        if (n == null || n.getPayload() == null) {
            return null;
        }
        try {
            StoredOverduePayload payload = objectMapper.readValue(n.getPayload(), StoredOverduePayload.class);
            return payload == null ? null : payload.subjectUserId();
        } catch (Exception e) {
            return null;
        }
    }

    private UserNotification findLatestOverdueForSubject(UUID subjectUserId) {
        if (subjectUserId == null) {
            return null;
        }
        List<UserNotification> recent = dataManager.load(UserNotification.class)
                .query("select n from UserNotification n where n.kind = :kind order by n.createdAt desc")
                .parameter("kind", UserNotificationKind.OVERDUE)
                .maxResults(200)
                .list();

        for (UserNotification n : recent) {
            if (subjectUserId.equals(tryExtractSubjectUserId(n))) {
                return n;
            }
        }
        return null;
    }

    private String fingerprintFromStoredPayload(UserNotification n) {
        if (n == null || n.getPayload() == null) {
            return "";
        }
        try {
            StoredOverduePayload payload = objectMapper.readValue(n.getPayload(), StoredOverduePayload.class);
            if (payload == null) {
                return "";
            }
            return fingerprint(payload.subjectUserId(), payload.items());
        } catch (Exception e) {
            return "";
        }
    }

    private static String fingerprint(UUID subjectUserId, List<OverdueItemDto> items) {
        if (subjectUserId == null || items == null || items.isEmpty()) {
            return "";
        }
        // Canonicalize: sort by type, then date
        List<OverdueItemDto> copy = new ArrayList<>(items);
        copy.sort(Comparator
                .comparing((OverdueItemDto i) -> i.type() == null ? "" : i.type().name())
                .thenComparing(i -> i.date() == null ? "" : i.date().toString()));

        StringBuilder sb = new StringBuilder();
        sb.append(subjectUserId);
        for (OverdueItemDto i : copy) {
            sb.append('|')
                    .append(i.type() == null ? "null" : i.type().name())
                    .append('@')
                    .append(i.date() == null ? "null" : i.date());
        }
        return sb.toString();
    }

    private void syncRecipients(UserNotification notification, Set<UUID> desiredRecipientUserIds) {
        if (notification == null || desiredRecipientUserIds == null || desiredRecipientUserIds.isEmpty()) {
            return;
        }

        List<UserNotificationRecipient> existing = dataManager.load(UserNotificationRecipient.class)
                .query("select r from UserNotificationRecipient r where r.notification.id = :nid")
                .parameter("nid", notification.getId())
                .list();

        Set<UUID> existingUserIds = new HashSet<>();
        for (UserNotificationRecipient r : existing) {
            if (r.getUser() != null && r.getUser().getId() != null) {
                existingUserIds.add(r.getUser().getId());
            }
        }

        // Remove recipients no longer applicable (department change, etc.)
        for (UserNotificationRecipient r : existing) {
            UUID uid = r.getUser() == null ? null : r.getUser().getId();
            if (uid == null || !desiredRecipientUserIds.contains(uid)) {
                dataManager.remove(r);
            }
        }

        // Add missing recipients
        for (UUID uid : desiredRecipientUserIds) {
            if (existingUserIds.contains(uid)) {
                continue;
            }
            User u = dataManager.load(User.class).id(uid).one();
            UserNotificationRecipient r = metadata.create(UserNotificationRecipient.class);
            r.setNotification(notification);
            r.setUser(u);
            dataManager.save(r);
        }
    }

    public record StoredOverduePayload(UUID subjectUserId, List<OverdueItemDto> items) {
    }
}

