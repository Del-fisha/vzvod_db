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

import java.time.LocalTime;
import java.util.UUID;

/**
 * Проверка маршрута командиром в рамках смены типа {@link TypeOfShift#CHECKING}.
 * <ul>
 *   <li>{@link #shift} — смена проверки (id смены CHECKING)</li>
 *   <li>{@link #serviceInfo} — кто зафиксировал проверку</li>
 *   <li>{@link #routeNumber} — маршрут ({@link NumberOfShift#getId()}), который проверили</li>
 *   <li>{@link #checkedAt} — время проверки</li>
 * </ul>
 */
@Setter
@Getter
@JmixEntity
@Table(name = "ROUTE_CHECK", indexes = {
        @Index(name = "IDX_ROUTE_CHECK_SHIFT", columnList = "SHIFT_ID"),
        @Index(name = "IDX_ROUTE_CHECK_ROUTE", columnList = "ROUTE_NUMBER")
})
@Entity
public class RouteCheck {

    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SHIFT_ID", nullable = false)
    private Shift shift;

    /** При удалении сотрудника проверка сохраняется (ссылка обнуляется). */
    @OnDeleteInverse(DeletePolicy.UNLINK)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "SERVICE_INFO_ID")
    private ServiceInfo serviceInfo;

    /**
     * Как у {@link Shift#number}: в БД строка id {@link NumberOfShift}, в коде enum.
     */
    @Column(name = "ROUTE_NUMBER", nullable = false)
    private String routeNumber;

    public NumberOfShift getRouteNumber() {
        return routeNumber == null ? null : NumberOfShift.fromId(routeNumber);
    }

    public void setRouteNumber(final NumberOfShift route) {
        this.routeNumber = route == null ? null : route.getId();
    }

    public void setRouteNumberId(final String routeId) {
        this.routeNumber = routeId;
    }

    public String getRouteNumberId() {
        return routeNumber;
    }

    @Column(name = "CHECKED_AT", nullable = false)
    private LocalTime checkedAt;

    @InstanceName
    @DependsOnProperties({"routeNumber", "checkedAt"})
    public String getInstanceName() {
        String route = routeNumber == null ? "" : routeNumber;
        String time = checkedAt == null ? "" : checkedAt.toString();
        if (route.isBlank() && time.isBlank()) {
            return "";
        }
        if (route.isBlank()) {
            return time;
        }
        if (time.isBlank()) {
            return route;
        }
        return route + " · " + time;
    }
}
