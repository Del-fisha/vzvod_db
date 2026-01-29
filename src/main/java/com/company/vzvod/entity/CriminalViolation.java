package com.company.vzvod.entity;

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Table(name = "CriminalViolation")
@JmixEntity
@Entity
public class CriminalViolation extends Violation {

    @InstanceName
    @Column(name = "TYPE_OF_CRIMINAL")
    private Integer type;

    public void setType(TypeOfCriminal type) {
        this.type = type == null ? null : type.getId();
    }

    public TypeOfCriminal getType() {
        return type == null ? null : TypeOfCriminal.fromId(type);
    }

}