package com.company.vzvod.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum StatusInService implements EnumClass<Integer> {

    ACTIVE(10),
    VOCATION(20),
    STUDY_LEAVE(30),
    PTC(40),
    BUSINESS_TRIP(50),
    SICK_LEAVE(60);

    private final Integer id;

    StatusInService(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    @Nullable
    public static StatusInService fromId(Integer id) {
        for (StatusInService at : StatusInService.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}