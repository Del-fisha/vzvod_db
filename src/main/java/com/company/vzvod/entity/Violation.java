package com.company.vzvod.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@JmixEntity
@MappedSuperclass
@Getter
@Setter
public class Violation {

    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "IMPACT")
    private Impact impact;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SHIFT_ID")
    private Shift shift;

    public void setId(UUID id) {
        this.id = id;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public void setImpact(Impact impact) {
        this.impact = impact;
    }
}