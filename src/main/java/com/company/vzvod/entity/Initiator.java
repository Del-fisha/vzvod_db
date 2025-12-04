package com.company.vzvod.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum Initiator implements EnumClass<String> {

    METRO("U"),
    GU("G"),
    MVD("M");

    private final String id;

    Initiator(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static Initiator fromId(String id) {
        for (Initiator at : Initiator.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}