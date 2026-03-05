package com.company.vzvod.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Setter
@Getter
@JmixEntity
@Table(name = "SHIFT")
@Entity
public class Shift {

    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "NUMBER")
    private NumberOfShift number;

    @Column(name = "TYPE_OF_SHIFT")
    private TypeOfShift typeOfShift;

    @ManyToMany
    @JoinTable(name = "SHIFT_SERVICE_INFO",
            joinColumns = @JoinColumn(name = "SHIFT_ID"),
            inverseJoinColumns = @JoinColumn(name = "SERVICE_INFO_ID"))
    private Set<ServiceInfo> units = new HashSet<>();;

    @Column(name = "DEPARTMENT")
    private Dep departmentToday;

    @Column(name = "DATE")
    private LocalDate date;

    @Column(name = "START_TIME")
    private LocalTime startTime;

    @Column(name = "END_TIME")
    private LocalTime endTime;

    @OnDeleteInverse(DeletePolicy.UNLINK)
    @OneToMany(mappedBy = "shift")
    private Set<CriminalViolation> criminalViolations;

    @OnDeleteInverse(DeletePolicy.UNLINK)
    @OneToMany(mappedBy = "shift")
    private Set<AdministrativeViolation> administrativeViolations;

    @Column(name = "COUNT_OF_STATEMENTS")
    private Integer countOfStatements;

    @Column(name = "COUNT_OF_CLAIMS")
    private Integer countOfClaims;

    @Column(name = "IBD_WITH_MIGRANT")
    private Integer ibdWithMigrant;

    @Column(name = "IBD_WITHOUT_MIGRANT")
    private Integer ibdWithoutMigrant;


    @InstanceName
    public String getInstanceName() {
        StringBuilder stringBuilder = new StringBuilder();
        for (ServiceInfo serviceInfo : getUnits()) {
            stringBuilder.append(serviceInfo.getUser().getLastName())
                    .append(" ")
                    .append(serviceInfo.getUser().getFirstName())
                    .append(", ");
        }
        return String.format("%s (%s) %s",
                this.date.format(DateTimeFormatter.ofPattern("yyyy dd MMMM")), this.number.getId(), stringBuilder);
    }
}