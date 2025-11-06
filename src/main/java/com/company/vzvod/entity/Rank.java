package com.company.vzvod.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum Rank implements EnumClass<String> {

    INTERN("A"),
    JUNIOR_SERGEANT("B"),
    SERGEANT("C"),
    SENIOR_SERGEANT("D"),
    PETTY_OFFICER("E"),
    ENSIGN("F"),
    SENIOR_WARRANT_OFFICER("G"),
    JUNIOR_LIEUTENANT("H"),
    LIEUTENANT("I"),
    SENIOR_LIEUTENANT("J"),
    CAPTAIN("K"),
    MAJOR("L"),
    LIEUTENANT_COLONEL("M"),
    COLONEL("N");

    private final String id;

    Rank(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static Rank fromId(String id) {
        for (Rank at : Rank.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
