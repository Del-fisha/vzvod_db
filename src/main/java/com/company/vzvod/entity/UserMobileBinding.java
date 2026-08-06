package com.company.vzvod.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Привязка мобильного клиента (Android) к учётной записи. Токен сессии — opaque.
 */
@Getter
@Setter
@JmixEntity
@Entity
@Table(name = "USER_MOBILE_BINDING")
public class UserMobileBinding {

    @Id
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false, unique = true)
    private User user;

    /** Opaque session token (хранится как есть; доступ только по HTTPS/локальной сети). */
    @Column(name = "TOKEN", nullable = false, unique = true, length = 64)
    private String token;

    @Column(name = "DEVICE_ID", length = 128)
    private String deviceId;

    @Column(name = "REGISTERED_AT", nullable = false)
    private OffsetDateTime registeredAt;

    @Column(name = "LAST_SEEN_AT")
    private OffsetDateTime lastSeenAt;
}
