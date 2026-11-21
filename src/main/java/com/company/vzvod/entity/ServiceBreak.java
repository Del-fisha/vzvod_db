package com.company.vzvod.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@JmixEntity
@Table(name = "SERVICE_BREAK", indexes = {
        @Index(name = "IDX_SERVICE_BREAK_SERVICE_INFO", columnList = "SERVICE_INFO_ID")
})
@Entity
@Getter
@Setter
public class ServiceBreak {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @NotNull
    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "SERVICE_INFO_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ServiceInfo serviceInfo;

    @NotNull(message = "{msg://com.company.vzvod.entity/ServiceBreak.fromDate.validation.NotNull}")
    @PastOrPresent(message = "{msg://com.company.vzvod.entity/ServiceBreak.fromDate.validation.PastOrPresent}")
    @Column(name = "FROM_DATE", nullable = false)
    private LocalDate fromDate;

    @NotNull(message = "{msg://com.company.vzvod.entity/ServiceBreak.toDate.validation.NotNull}")
    @PastOrPresent(message = "{msg://com.company.vzvod.entity/ServiceBreak.toDate.validation.PastOrPresent}")
    @Column(name = "TO_DATE", nullable = false)
    private LocalDate toDate;

    @InstanceName
    @DependsOnProperties({"fromDate", "toDate"})
    public String getInstanceName() {
        String from = fromDate == null ? "" : DATE.format(fromDate);
        String to = toDate == null ? "" : DATE.format(toDate);
        return from + " — " + to;
    }
}
