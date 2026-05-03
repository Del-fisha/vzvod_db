package com.company.vzvod.entity;

import io.jmix.core.MetadataTools;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.datatype.DatatypeFormatter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@JmixEntity
@Table(name = "DELETED_EVENT")
@Entity
@Getter
@Setter
public class DeletedEvent {

    @JmixGeneratedValue
    @Id
    @Column(name = "ID", nullable = false)
    private UUID id;

    @Column(name = "ORIGINAL_EVENT_ID")
    private UUID originalEventId;

    @Column(name = "EVENT_TYPE")
    private String eventType;

    @Column(name = "PLACE", length = 30)
    private String place;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DATE_")
    private LocalDate date;

    @Column(name = "TIME_")
    private LocalTime time;

    @Column(name = "SHIFT_OF_DEPARTMENT")
    private Integer shiftOfDepartment;

    @Column(name = "DESCRIPTION")
    @Lob
    private String description;

    /** false — запись только для блокировки импорта (Kafka), не показывается в «Без взвода». */
    @Column(name = "RESTORABLE", nullable = false)
    private Boolean restorable = true;

    @Transient
    @Nullable
    public EventType getEventType() {
        return eventType == null ? null : EventType.fromId(eventType);
    }

    public void setEventType(@Nullable EventType type) {
        this.eventType = type == null ? null : type.getId();
    }

    @InstanceName
    @DependsOnProperties({"name", "date", "time"})
    public String getInstanceName(MetadataTools metadataTools, DatatypeFormatter datatypeFormatter) {
        return String.format("%s %s %s",
                metadataTools.format(name),
                datatypeFormatter.formatLocalDate(date),
                datatypeFormatter.formatLocalTime(time));
    }
}