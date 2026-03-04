package com.company.vzvod.entity;

import com.company.vzvod.service.PhoneNormalizer;
import io.jmix.core.DeletePolicy;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@JmixEntity
@Table(name = "CONTACTS", indexes = {
        @Index(name = "IDX_CONTACTS_REGISTRATION", columnList = "REGISTRATION_ID"),
        @Index(name = "IDX_CONTACTS_HABITATION", columnList = "HABITATION_ID")
})
@Entity
@Getter
@Setter
public class Contacts {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "PHONE_NUMBER", length = 28)
    private String phoneNumber;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @JoinColumn(name = "REGISTRATION_ID")
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Address registration;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @JoinColumn(name = "HABITATION_ID")
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Address habitation;

    @Column(name = "NEAREST_METRO_STATION")
    private Integer nearestMetroStation;

    public void setNearestMetroStation(MetroStation nearestMetroStation) {
        this.nearestMetroStation = nearestMetroStation == null ? null : nearestMetroStation.getId();
    }

    public MetroStation getNearestMetroStation() {
        return nearestMetroStation == null ? null : MetroStation.fromId(nearestMetroStation);
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = new PhoneNormalizer().normalize(phoneNumber);
    }

    @InstanceName
    @DependsOnProperties({"phoneNumber"})
    public String getInstanceName(MetadataTools metadataTools) {
        return metadataTools.format(phoneNumber);
    }
}