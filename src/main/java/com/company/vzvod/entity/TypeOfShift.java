package com.company.vzvod.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;


public enum TypeOfShift implements EnumClass<String> {

    BAT_POST("BAT_POST"),
    VZVOD_ROUTE("VZVOD_ROUTE"),
    STRENGTHENING("Strengthening");

    private final String id;

    TypeOfShift(String id) {
        this.id = id;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @Nullable
    public static TypeOfShift fromId(String id) {
        for (TypeOfShift at : TypeOfShift.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}