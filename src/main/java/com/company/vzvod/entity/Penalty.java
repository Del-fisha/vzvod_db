package com.company.vzvod.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@JmixEntity
@Table(name = "PENALTY", indexes = {
        @Index(name = "IDX_PENALTY_USER_SERVICE_INFO", columnList = "USER_SERVICE_INFO_ID")
})
@Entity
@Getter
@Setter
public class Penalty {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_SERVICE_INFO_ID")
    private ServiceInfo userServiceInfo;

    @Column(name = "INITIATOR")
    private String initiator;

    @Column(name = "PENALTY_TYPE")
    private String penaltyType;

    @Column(name = "PENALTY_STATUS")
    private String penaltyStatus;

    @PastOrPresent(message = "{msg://com.company.vzvod.entity/Penalty.date.validation.PastOrPresent}")
    @Column(name = "DATE_")
    private LocalDate date;

    @Column(name = "ORDER_NUMBER", length = 40)
    private String orderNumber;

    @InstanceName
    @Column(name = "DESCRIPTION", length = 400)
    private String description;

    public PenaltyStatus getPenaltyStatus() {
        return penaltyStatus == null ? null : PenaltyStatus.fromId(penaltyStatus);
    }

    public void setPenaltyStatus(PenaltyStatus penaltyStatus) {
        this.penaltyStatus = penaltyStatus == null ? null : penaltyStatus.getId();
    }

    /**
     * If penalty is ACTIVE and at least a year has passed since {@link #date},
     * switch it to COMPLETED.
     *
     * @return true if status was changed
     */
    public boolean autoCompleteIfExpired(LocalDate now) {
        if (now == null) {
            now = LocalDate.now();
        }

        if (getPenaltyStatus() != PenaltyStatus.ACTIVE) {
            return false;
        }
        if (date == null) {
            return false;
        }

        // "a year has passed" => date <= now - 1 year
        if (date.isAfter(now.minusYears(1))) {
            return false;
        }

        setPenaltyStatus(PenaltyStatus.COMPLETED);
        return true;
    }

    public PenaltyType getPenaltyType() {
        return penaltyType == null ? null : PenaltyType.fromId(penaltyType);
    }

    public void setPenaltyType(PenaltyType penaltyType) {
        this.penaltyType = penaltyType == null ? null : penaltyType.getId();
    }

    public Initiator getInitiator() {
        return initiator == null ? null : Initiator.fromId(initiator);
    }

    public void setInitiator(Initiator initiator) {
        this.initiator = initiator == null ? null : initiator.getId();
    }
}