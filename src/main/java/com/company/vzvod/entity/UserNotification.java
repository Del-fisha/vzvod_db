package com.company.vzvod.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@JmixEntity
@Entity
@Table(name = "USER_NOTIFICATION", indexes = {
        @Index(name = "IDX_USER_NOTIFICATION_CREATED_AT", columnList = "CREATED_AT"),
        @Index(name = "IDX_USER_NOTIFICATION_RESOLVED_AT", columnList = "RESOLVED_AT")
})
public class UserNotification {

    @Id
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    private UUID id;

    @Column(name = "KIND", nullable = false, length = 50)
    private String kind;

    /**
     * JSON payload for rendering (kept flexible for different notification kinds).
     */
    @Lob
    @Column(name = "PAYLOAD", nullable = false)
    private String payload;

    @Column(name = "CREATED_AT", nullable = false)
    private OffsetDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREATED_BY_USER_ID")
    private User createdByUser;

    @Column(name = "RESOLVED_AT")
    private OffsetDateTime resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESOLVED_BY_USER_ID")
    private User resolvedByUser;
}

