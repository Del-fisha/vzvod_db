package com.company.vzvod.entity;

import io.jmix.core.MetadataTools;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

import com.company.vzvod.security.crypto.EncryptedStringConverter;

@JmixEntity
@Table(name = "ADDRESS")
@Entity
@Getter
@Setter
public class Address {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Pattern(message = "{msg://com.company.vzvod.entity/Address.index.validation.Pattern}", regexp = "^\\d{6}$")
    @Size(max = 6)
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "INDEX_", length = 6)
    private String index;

    @Column(name = "INDEX_", insertable = false, updatable = false)
    private String indexRaw;

    @Size(max = 50)
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "CITY", length = 50)
    private String city;

    @Column(name = "CITY", insertable = false, updatable = false)
    private String cityRaw;

    @Size(max = 255)
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "STREET")
    private String street;

    @Column(name = "STREET", insertable = false, updatable = false)
    private String streetRaw;

    @Size(max = 5)
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "HOUSE_NUMBER", length = 5)
    private String houseNumber;

    @Column(name = "HOUSE_NUMBER", insertable = false, updatable = false)
    private String houseNumberRaw;

    @Size(max = 4)
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "BODY_", length = 4)
    private String body;

    @Column(name = "BODY_", insertable = false, updatable = false)
    private String bodyRaw;

    @Size(max = 10)
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "FLAT", length = 10)
    private String flat;

    @Column(name = "FLAT", insertable = false, updatable = false)
    private String flatRaw;

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

    @InstanceName
    @DependsOnProperties({"city", "street", "houseNumber", "body", "flat"})
    public String getInstanceName() {
        return String.format("%s, %s, %s, %s, %s",
                        city != null ? city : "",
                        street != null ? street : "",
                        houseNumber != null ? houseNumber : "",
                        body != null ? body : "",
                        flat != null ? flat : "")
                .replaceAll(", +", ", ")
                .replaceAll("^, |, $", "");
    }

    public String getIndexRaw() {
        return indexRaw;
    }

    public String getCityRaw() {
        return cityRaw;
    }

    public String getStreetRaw() {
        return streetRaw;
    }

    public String getHouseNumberRaw() {
        return houseNumberRaw;
    }

    public String getBodyRaw() {
        return bodyRaw;
    }

    public String getFlatRaw() {
        return flatRaw;
    }
}