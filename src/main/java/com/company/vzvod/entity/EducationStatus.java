package com.company.vzvod.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum EducationStatus implements EnumClass<String> {

    AT_THE_MOMENT("A"),
    FINISHED("B");

    private final String id;

    EducationStatus(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static EducationStatus fromId(String id) {
        for (EducationStatus at : EducationStatus.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}