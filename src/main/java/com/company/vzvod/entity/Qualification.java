package com.company.vzvod.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum Qualification implements EnumClass<Integer> {

    NONE(0),
    FIRST(1),
    SECOND(2),
    THIRD(3),
    MASTER(4);

    private final Integer id;

    Qualification(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    @Nullable
    public static Qualification fromId(Integer id) {
        for (Qualification at : Qualification.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}