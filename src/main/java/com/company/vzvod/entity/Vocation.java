package com.company.vzvod.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@JmixEntity
@Table(name = "VOCATION")
@Entity
@Getter
@Setter
public class Vocation {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "USER_SERVICE_INFO_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private ServiceInfo userServiceInfo;

    @Column(name = "VOCATION_TYPE")
    private Integer typeId;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(name = "COUNT_OF_DAYS")
    private Integer countOfDays;

    @Column(name = "HAS_DEPARTURE")
    private Boolean hasDeparture;

    @Column(name = "CITY_TO_DRIVE")
    private String cityToDrive;

    @Column(name = "DAYS_ADDED_BY_DEPARTURE")
    private Integer daysAddedByDeparture;

    @JmixProperty
    @DependsOnProperties("typeId")
    public VocationType getType() {
        return typeId == null ? null : VocationType.fromId(typeId);
    }

    public void setType(VocationType type) {
        this.typeId = type == null ? null : type.getId();
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public void setUserServiceInfo(ServiceInfo userServiceInfo) {
        this.userServiceInfo = userServiceInfo;
    }


    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setCountOfDays(Integer countOfDays) {
        this.countOfDays = countOfDays;
    }

    public Boolean isHasDeparture() {
        return hasDeparture;
    }

    public void setHasDeparture(Boolean hasDeparture) {
        this.hasDeparture = hasDeparture;
    }

    public void setCityToDrive(String cityToDrive) {
        this.cityToDrive = cityToDrive;
    }

    public void setDaysAddedByDeparture(Integer daysAddedByDeparture) {
        this.daysAddedByDeparture = daysAddedByDeparture;
    }

    public void setId(UUID id) {
        this.id = id;
    }

}