package com.company.vzvod.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Привязка учётной записи сотрудника к Telegram-чату (основная БД Vzvod).
 */
@Getter
@Setter
@JmixEntity
@Entity
@Table(name = "USER_TELEGRAM_BINDING")
public class UserTelegramBinding {

    @Id
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false, unique = true)
    private User user;

    @Column(name = "CHAT_ID", nullable = false, unique = true)
    private Long chatId;

    @Column(name = "REGISTERED_AT", nullable = false)
    private OffsetDateTime registeredAt;
}
