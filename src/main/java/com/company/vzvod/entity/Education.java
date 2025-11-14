package com.company.vzvod.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.util.UUID;

@JmixEntity
@Table(name = "EDUCATION")
@Entity
public class Education {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @PastOrPresent(message = "{msg://com.company.vzvod.entity/Education.started.validation.PastOrPresent}")
    @Column(name = "STARTED")
    private LocalDate started;

    @Column(name = "UNTIL_")
    private LocalDate until;

    @Column(name = "TYPE_")
    private String type;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "NAME_OF_INSTITUTION")
    private String nameOfInstitution;

    public String getNameOfInstitution() {
        return nameOfInstitution;
    }

    public void setNameOfInstitution(String nameOfInstitution) {
        this.nameOfInstitution = nameOfInstitution;
    }

    public EducationStatus getStatus() {
        return status == null ? null : EducationStatus.fromId(status);
    }

    public void setStatus(EducationStatus status) {
        this.status = status == null ? null : status.getId();
    }

    public TypeOfEducation getType() {
        return type == null ? null : TypeOfEducation.fromId(type);
    }

    public void setType(TypeOfEducation type) {
        this.type = type == null ? null : type.getId();
    }

    public LocalDate getUntil() {
        return until;
    }

    public void setUntil(LocalDate until) {
        this.until = until;
    }

    public LocalDate getStarted() {
        return started;
    }

    public void setStarted(LocalDate started) {
        this.started = started;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

}