package com.company.vzvod.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum IncentiveType implements EnumClass<Integer> {

    GRATITUDE(10),
    BONUS(20),
    DIPLOMA(30),
    MEDAL(40);

    private final Integer id;

    IncentiveType(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    @Nullable
    public static IncentiveType fromId(Integer id) {
        for (IncentiveType at : IncentiveType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}