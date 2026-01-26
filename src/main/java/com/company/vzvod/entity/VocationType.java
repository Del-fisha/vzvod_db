package com.company.vzvod.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum VocationType implements EnumClass<Integer> {

    MAIN(10),
    ADDITIONAL(20),
    PART_OF_MAIN(30);

    private final Integer id;

    VocationType(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    @Nullable
    public static VocationType fromId(Integer id) {
        for (VocationType at : VocationType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}