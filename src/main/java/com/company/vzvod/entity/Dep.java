package com.company.vzvod.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum Dep implements EnumClass<Integer> {

    FIRST(10),
    SECOND(20);

    private final Integer id;

    Dep(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    @Nullable
    public static Dep fromId(Integer id) {
        for (Dep at : Dep.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}