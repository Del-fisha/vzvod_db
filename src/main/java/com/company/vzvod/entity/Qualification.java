package com.company.vzvod.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum Qualification implements EnumClass<Integer> {

    /** Порядок по возрастанию классности (выпадающие списки и т.п.). */
    NONE(0),
    THIRD(3),
    SECOND(2),
    FIRST(1),
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