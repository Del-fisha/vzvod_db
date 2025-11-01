package com.company.vzvod.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@JmixEntity
@Table(name = "VEHICLE", indexes = {
        @Index(name = "IDX_VEHICLE_USER", columnList = "USER_ID")
})
@Entity
public class Vehicle {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "STATE_NUMBER", length = 10)
    private String stateNumber;

    @Column(name = "MODEL", length = 50)
    private String model;

    @Column(name = "BRAND", length = 50)
    private String brand;

    @Column(name = "REGISTRATION_CERTIFICATE")
    private String registrationCertificate;

    @Column(name = "INSURANCE")
    private LocalDate insurance;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "USER_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getInsurance() {
        return insurance;
    }

    public void setInsurance(LocalDate insurance) {
        this.insurance = insurance;
    }

    public String getRegistrationCertificate() {
        return registrationCertificate;
    }

    public void setRegistrationCertificate(String registrationCertificate) {
        this.registrationCertificate = registrationCertificate;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getStateNumber() {
        return stateNumber;
    }

    public void setStateNumber(String stateNumber) {
        this.stateNumber = stateNumber;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

}