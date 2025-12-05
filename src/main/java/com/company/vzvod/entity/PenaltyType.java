package com.company.vzvod.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum PenaltyType implements EnumClass<String> {

    REMARK("R"),
    REPRIMAND("RP"),
    SEVERE_REPRIMAND("SR"),
    INADEQUATE_SERVICE("IS"),
    DISMISSAL("D"),
    DEMOTION("DM");

    private final String id;

    PenaltyType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static PenaltyType fromId(String id) {
        for (PenaltyType at : PenaltyType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}