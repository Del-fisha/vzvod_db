package com.company.vzvod.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.util.UUID;

@JmixEntity
@Table(name = "CONTACTS", indexes = {
        @Index(name = "IDX_CONTACTS_REGISTRATION", columnList = "REGISTRATION_ID"),
        @Index(name = "IDX_CONTACTS_HABITATION", columnList = "HABITATION_ID")
})
@Entity
public class Contacts {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "PHONE_NUMBER", length = 28)
    private String phoneNumber;

    @JoinColumn(name = "REGISTRATION_ID")
    @OneToOne(fetch = FetchType.LAZY)
    private Address registration;

    @JoinColumn(name = "HABITATION_ID")
    @OneToOne(fetch = FetchType.LAZY)
    private Address habitation;

    @Column(name = "NEAREST_METRO_STATION")
    private Integer nearestMetroStation;

    public void setNearestMetroStation(MetroStation nearestMetroStation) {
        this.nearestMetroStation = nearestMetroStation == null ? null : nearestMetroStation.getId();
    }

    public MetroStation getNearestMetroStation() {
        return nearestMetroStation == null ? null : MetroStation.fromId(nearestMetroStation);
    }

    public Address getHabitation() {
        return habitation;
    }

    public void setHabitation(Address habitation) {
        this.habitation = habitation;
    }

    public Address getRegistration() {
        return registration;
    }

    public void setRegistration(Address registration) {
        this.registration = registration;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

}