package com.company.vzvod.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum TypeOfHousing implements EnumClass<String> {

    FLAT("A"),
    ROOM("B"),
    HOUSE("C");

    private final String id;

    TypeOfHousing(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static TypeOfHousing fromId(String id) {
        for (TypeOfHousing at : TypeOfHousing.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}