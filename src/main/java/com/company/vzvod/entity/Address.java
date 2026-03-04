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
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

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
}