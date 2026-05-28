package com.company.vzvod.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
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

    /**
     * Как {@link User#getGender()}: в БД строка id перечисления ({@link NumberOfShift#getId()}), в коде {@link NumberOfShift}.
     * Иначе EclipseLink ошибочно вешает числовой конвертер на enum-поле и падает на «МП 30».
     */
    @Column(name = "NUMBER")
    private String number;

    public NumberOfShift getNumber() {
        return number == null ? null : NumberOfShift.fromId(number);
    }

    public void setNumber(final NumberOfShift routeNumber) {
        this.number = routeNumber == null ? null : routeNumber.getId();
    }

    @Column(name = "TYPE_OF_SHIFT")
    private String typeOfShift;

    public TypeOfShift getTypeOfShift() {
        return typeOfShift == null ? null : TypeOfShift.fromId(typeOfShift);
    }

    public void setTypeOfShift(final TypeOfShift type) {
        this.typeOfShift = type == null ? null : type.getId();
    }

    @ManyToMany
    @JoinTable(name = "SHIFT_SERVICE_INFO",
            joinColumns = @JoinColumn(name = "SHIFT_ID"),
            inverseJoinColumns = @JoinColumn(name = "SERVICE_INFO_ID"))
    private Set<ServiceInfo> units = new HashSet<>();

    /**
     * В БД — integer id {@link Dep#getId()} (1 или 2); в коде — {@link Dep}.
     * Иначе EclipseLink ObjectTypeConverter для {@link Dep} даёт «No conversion value … for [2]».
     */
    @Column(name = "DEPARTMENT")
    private Integer departmentToday;

    public Dep getDepartmentToday() {
        return departmentToday == null ? null : Dep.fromId(departmentToday);
    }

    public void setDepartmentToday(final Dep dep) {
        this.departmentToday = dep == null ? null : dep.getId();
    }

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
    @DependsOnProperties({"number", "date"})
    public String getInstanceName() {
        NumberOfShift route = getNumber();
        if (date == null && route == null) {
            return "";
        }
        var parts = new StringBuilder();
        if (date != null) {
            parts.append(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (route != null) {
            if (parts.length() > 0) {
                parts.append(" · ");
            }
            parts.append(route.getId());
        }
        return parts.toString();
    }
}