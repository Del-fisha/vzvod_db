package com.company.vzvod.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum PenaltyStatus implements EnumClass<String> {

    ACTIVE("A"),
    REMOVED("B"),
    COMPLETED("C");

    private final String id;

    PenaltyStatus(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static PenaltyStatus fromId(String id) {
        for (PenaltyStatus at : PenaltyStatus.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}