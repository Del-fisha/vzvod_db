package com.company.vzvod.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.util.UUID;

@JmixEntity
@Table(name = "VOCATION")
@Entity
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
    private VocationType type;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(name = "COUNT_OF_DAYS")
    private Integer countOfDays;

    @Column(name = "ALL_REMAIND_DAYS")
    private Integer allRemaindDays;

    @Column(name = "HAS_DEPARTURE")
    private Boolean hasDeparture;

    @Column(name = "CITY_TO_DRIVE")
    private String cityToDrive;

    @Column(name = "DAYS_ADDED_BY_DEPARTURE")
    private Integer daysAddedByDeparture;

    public ServiceInfo getUserServiceInfo() {
        return userServiceInfo;
    }

    public void setUserServiceInfo(ServiceInfo userServiceInfo) {
        this.userServiceInfo = userServiceInfo;
    }

    public VocationType getType() {
        return type;
    }

    public void setType(VocationType type) {
        this.type = type;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getCountOfDays() {
        return countOfDays;
    }

    public void setCountOfDays(Integer countOfDays) {
        this.countOfDays = countOfDays;
    }

    public Integer getAllRemaindDays() {
        return allRemaindDays;
    }

    public void setAllRemaindDays(Integer allRemaindDays) {
        this.allRemaindDays = allRemaindDays;
    }

    public Boolean isHasDeparture() {
        return hasDeparture;
    }

    public void setHasDeparture(Boolean hasDeparture) {
        this.hasDeparture = hasDeparture;
    }

    public String getCityToDrive() {
        return cityToDrive;
    }

    public void setCityToDrive(String cityToDrive) {
        this.cityToDrive = cityToDrive;
    }

    public Integer getDaysAddedByDeparture() {
        return daysAddedByDeparture;
    }

    public void setDaysAddedByDeparture(Integer daysAddedByDeparture) {
        this.daysAddedByDeparture = daysAddedByDeparture;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

}