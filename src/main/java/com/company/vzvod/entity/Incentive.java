package com.company.vzvod.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.util.UUID;

@JmixEntity
@Table(name = "INCENTIVE", indexes = {
        @Index(name = "IDX_INCENTIVE_USER_SERVICE_INFO", columnList = "USER_SERVICE_INFO_ID")
})
@Entity
public class Incentive {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "USER_SERVICE_INFO_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private ServiceInfo userServiceInfo;

    @Column(name = "INITIATOR")
    private String initiator;

    @Column(name = "INCENTIVE_TYPE")
    private Integer incentiveType;

    @PastOrPresent(message = "{msg://com.company.vzvod.entity/Incentive.date.validation.PastOrPresent}")
    @Column(name = "DATE_")
    private LocalDate date;

    @Column(name = "ORDER_NUMBER", length = 40)
    private String orderNumber;

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

    public IncentiveType getIncentiveType() {
        return incentiveType == null ? null : IncentiveType.fromId(incentiveType);
    }

    public void setIncentiveType(IncentiveType incentiveType) {
        this.incentiveType = incentiveType == null ? null : incentiveType.getId();
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

    @InstanceName
    @DependsOnProperties({"incentiveType"})
    public String getInstanceName(MetadataTools metadataTools) {
        return metadataTools.format(getIncentiveType());
    }
}