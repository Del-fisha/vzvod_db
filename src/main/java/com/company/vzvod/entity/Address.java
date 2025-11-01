package com.company.vzvod.entity;

import io.jmix.core.MetadataTools;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

@JmixEntity
@Table(name = "ADDRESS")
@Entity
public class Address {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Pattern(message = "{msg://com.company.vzvod.entity/Address.index.validation.Pattern}", regexp = "^\\d{6}$")
    @Column(name = "INDEX_", length = 6)
    private String index;

    @Column(name = "CITY", length = 50)
    private String city;

    @Column(name = "STREET")
    private String street;

    @Column(name = "HOUSE_NUMBER", length = 5)
    private String houseNumber;

    @Column(name = "BODY_", length = 4)
    private String body;

    @Column(name = "FLAT", length = 10)
    private String flat;

    @Column(name = "TYPE_OF_HOUSING")
    private String typeOfHousing;

    @Column(name = "STATUS_OF_HOUSING")
    private String statusOfHousing;

    public StatusOfHousing getStatusOfHousing() {
        return statusOfHousing == null ? null : StatusOfHousing.fromId(statusOfHousing);
    }

    public void setStatusOfHousing(StatusOfHousing statusOfHousing) {
        this.statusOfHousing = statusOfHousing == null ? null : statusOfHousing.getId();
    }

    public TypeOfHousing getTypeOfHousing() {
        return typeOfHousing == null ? null : TypeOfHousing.fromId(typeOfHousing);
    }

    public void setTypeOfHousing(TypeOfHousing typeOfHousing) {
        this.typeOfHousing = typeOfHousing == null ? null : typeOfHousing.getId();
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getFlat() {
        return flat;
    }

    public void setFlat(String flat) {
        this.flat = flat;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @InstanceName
    @DependsOnProperties({"city", "street", "houseNumber", "body", "flat"})
    public String getInstanceName(MetadataTools metadataTools) {
        return String.format("%s %s %s %s %s",
                metadataTools.format(city),
                metadataTools.format(street),
                metadataTools.format(houseNumber),
                metadataTools.format(body),
                metadataTools.format(flat));
    }
}