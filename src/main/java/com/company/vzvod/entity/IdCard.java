package com.company.vzvod.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.util.UUID;

@JmixEntity
@Table(name = "ID_CARD")
@Entity
public class IdCard {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "SPL", length = 6)
    private String spl;

    @PastOrPresent(message = "{msg://com.company.vzvod.entity/IdCard.issued.validation.PastOrPresent}")
    @Column(name = "ISSUED", nullable = false)
    private LocalDate issued;

    @FutureOrPresent(message = "{msg://com.company.vzvod.entity/IdCard.until.validation.FutureOrPresent}")
    @Column(name = "UNTIL_", nullable = false)
    private LocalDate until;

    public LocalDate getUntil() {
        return until;
    }

    public void setUntil(LocalDate until) {
        this.until = until;
    }

    public LocalDate getIssued() {
        return issued;
    }

    public void setIssued(LocalDate issued) {
        this.issued = issued;
    }

    public String getSpl() {
        return spl;
    }

    public void setSpl(String spl) {
        this.spl = spl;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

}