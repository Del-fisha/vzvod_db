package com.company.vzvod.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@JmixEntity
@Entity
@Table(name = "USER_NOTIFICATION_RECIPIENT", indexes = {
        @Index(name = "IDX_UNR_USER", columnList = "USER_ID"),
        @Index(name = "IDX_UNR_NOTIFICATION", columnList = "NOTIFICATION_ID"),
        @Index(name = "IDX_UNR_USER_NOTIFICATION", columnList = "USER_ID, NOTIFICATION_ID", unique = true)
})
public class UserNotificationRecipient {

    @Id
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "NOTIFICATION_ID", nullable = false)
    private UserNotification notification;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;
}

