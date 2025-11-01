package com.company.vzvod.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum TypeOfEducation implements EnumClass<String> {

    SCHOOL_9("A"),
    SCHOOL_11("B"),
    SPECIFIC("C"),
    UNIVERSITY("D");

    private final String id;

    TypeOfEducation(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static TypeOfEducation fromId(String id) {
        for (TypeOfEducation at : TypeOfEducation.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}