package com.company.vzvod.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum Gender implements EnumClass<String> {

    MALE("M"),
    FEMALE("F");

    private final String id;

    Gender(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static Gender fromId(String id) {
        for (Gender gender : Gender.values()) {
            if (gender.getId().equals(id)) {
                return gender;
            }
        }
        return null;
    }
}