package com.company.vzvod.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Агрегат «все смены за операционный день»: одна строка на пару дата + отделение.
 */
@Setter
@Getter
@JmixEntity
@Table(name = "ALL_TODAY_SHIFTS", indexes = {
        @Index(name = "IDX_ALL_TODAY_SHIFTS_DATE_DEP", columnList = "DATE_, DEPARTMENT", unique = true)
})
@Entity
public class AllTodayShifts {

    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "DATE_")
    private LocalDate date;

    /**
     * В БД — integer id {@link Dep#getId()}; в коде — {@link Dep}.
     * Тот же приём, что у {@link Shift#getDepartmentToday()}.
     */
    @Column(name = "DEPARTMENT")
    private Integer department;

    public Dep getDepartment() {
        return department == null ? null : Dep.fromId(department);
    }

    public void setDepartment(final Dep dep) {
        this.department = dep == null ? null : dep.getId();
    }

    @InstanceName
    @DependsOnProperties({"date", "department"})
    public String getInstanceName() {
        if (date == null && getDepartment() == null) {
            return "";
        }
        StringBuilder parts = new StringBuilder();
        if (date != null) {
            parts.append(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        Dep dep = getDepartment();
        if (dep != null) {
            if (parts.length() > 0) {
                parts.append(" · ");
            }
            parts.append("Отд. ").append(dep.getId());
        }
        return parts.toString();
    }
}
