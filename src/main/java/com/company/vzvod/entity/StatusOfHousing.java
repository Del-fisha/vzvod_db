package com.company.vzvod.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum StatusOfHousing implements EnumClass<String> {

    OWNER("A"),
    SUBLEASED("B"),
    RENTED("C"),
    SHARED("D");

    private final String id;

    StatusOfHousing(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static StatusOfHousing fromId(String id) {
        for (StatusOfHousing at : StatusOfHousing.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}