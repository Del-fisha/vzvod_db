package com.company.vzvod.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@JmixEntity
@Table(name = "ANNUAL_MARK", indexes = {
        @Index(name = "IDX_ANNUAL_MARK_SERVICE_INFO", columnList = "SERVICE_INFO_ID")
}, uniqueConstraints = {
        @UniqueConstraint(name = "UK_ANNUAL_MARK_SI_KIND_YEAR", columnNames = {"SERVICE_INFO_ID", "KIND", "MARK_YEAR"})
})
@Entity
@Getter
@Setter
public class AnnualMark {

    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @NotNull
    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "SERVICE_INFO_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ServiceInfo serviceInfo;

    @NotNull
    @Column(name = "KIND", nullable = false, length = 32)
    private String kind;

    @NotNull
    @Column(name = "MARK_YEAR", nullable = false)
    private Integer markYear;

    @NotNull
    @Column(name = "CHECKED", nullable = false)
    private Boolean checked = false;

    @Column(name = "PERFORMED_ON")
    private LocalDate performedOn;

    @Column(name = "VALID_UNTIL")
    private LocalDate validUntil;
}
