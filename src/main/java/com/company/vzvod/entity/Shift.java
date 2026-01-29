package com.company.vzvod.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

@JmixEntity
@Table(name = "SHIFT")
@Entity
public class Shift {

    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NUMBER_")
    private Integer number;

    @Column(name = "TYPE_OF_SHIFT")
    private String typeOfShift;


    private Set<ServiceInfo> units;

    public Set<ServiceInfo> getUnits() {
        return units;
    }

    public void setUnits(Set<ServiceInfo> units) {
        this.units = units;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Set<CriminalViolation> getCriminalViolations() {
        return criminalViolations;
    }

    public void setCriminalViolations(Set<CriminalViolation> criminalViolations) {
        this.criminalViolations = criminalViolations;
    }

    public Set<AdministrativeViolation> getAdministrativeViolations() {
        return administrativeViolations;
    }

    public void setAdministrativeViolations(Set<AdministrativeViolation> administrativeViolations) {
        this.administrativeViolations = administrativeViolations;
    }

    public Integer getCountOfStatements() {
        return countOfStatements;
    }

    public void setCountOfStatements(Integer countOfStatements) {
        this.countOfStatements = countOfStatements;
    }

    public Integer getCountOfClaims() {
        return countOfClaims;
    }

    public void setCountOfClaims(Integer countOfClaims) {
        this.countOfClaims = countOfClaims;
    }

    public Integer getIbdWithMigrant() {
        return ibdWithMigrant;
    }

    public void setIbdWithMigrant(Integer ibdWithMigrant) {
        this.ibdWithMigrant = ibdWithMigrant;
    }

    public Integer getIbdWithoutMigrant() {
        return ibdWithoutMigrant;
    }

    public void setIbdWithoutMigrant(Integer ibdWithoutMigrant) {
        this.ibdWithoutMigrant = ibdWithoutMigrant;
    }

    @InstanceName
    @Column(name = "DATE_")
    private LocalDate date;

    @Column(name = "START_TIME")
    private LocalTime startTime;

    @Column(name = "END_TIME")
    private LocalTime endTime;

    private Set<CriminalViolation> criminalViolations;

    private Set<AdministrativeViolation> administrativeViolations;

    @Column(name = "COUNT_OF_STATEMENTS")
    private Integer countOfStatements;

    @Column(name = "COUNT_OF_CLAIMS")
    private Integer countOfClaims;

    @Column(name = "IBD_WITH_MIGRANT")
    private Integer ibdWithMigrant;

    @Column(name = "IBD_WITHOUT_MIGRANT")
    private Integer ibdWithoutMigrant;

    public String getTypeOfShift() {
        return typeOfShift;
    }

    public void setTypeOfShift(TypeOfShift typeOfShift) {
        this.typeOfShift = typeOfShift == null ? null : typeOfShift.toString();
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(NumberOfShift number) {
        this.number = number == null ? null : number.getId();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @InstanceName
    public String getInstanceName() {
        return String.format("%s %s",
                this.date, this.number.toString());
    }


}