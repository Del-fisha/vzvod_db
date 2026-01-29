package com.company.vzvod.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum Impact implements EnumClass<Integer> {

    WITHOUT_IMPACT(0),
    PHYSICAL_FORCE(1),
    SPECIAL_TOOLS(2),
    WEAPON(3)
    ;

    private final Integer id;

    Impact(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    @Nullable
    public static Impact fromId(Integer id) {
        for (Impact at : Impact.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}