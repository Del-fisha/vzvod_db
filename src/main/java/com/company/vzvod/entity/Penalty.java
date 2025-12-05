package com.company.vzvod.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.util.UUID;

@JmixEntity
@Table(name = "PENALTY", indexes = {
        @Index(name = "IDX_PENALTY_USER_SERVICE_INFO", columnList = "USER_SERVICE_INFO_ID")
})
@Entity
public class Penalty {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @ManyToOne
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public PenaltyStatus getPenaltyStatus() {
        return penaltyStatus == null ? null : PenaltyStatus.fromId(penaltyStatus);
    }

    public void setPenaltyStatus(PenaltyStatus penaltyStatus) {
        this.penaltyStatus = penaltyStatus == null ? null : penaltyStatus.getId();
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

    public ServiceInfo getUserServiceInfo() {
        return userServiceInfo;
    }

    public void setUserServiceInfo(ServiceInfo userServiceInfo) {
        this.userServiceInfo = userServiceInfo;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

}