package com.company.vzvod.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;


public enum NumberOfShift implements EnumClass<Integer> {

    _28(28),
    _30(30),
    _31(31),
    _32(32),
    _5(5),
    _6(6),
    _3(3),
    _18(18),
    ANOTHER(999);

    private final Integer id;

    NumberOfShift(Integer id) {
        this.id = id;
    }

    @NonNull
    public Integer getId() {
        return id;
    }

    @Nullable
    public static NumberOfShift fromId(Integer id) {
        for (NumberOfShift at : NumberOfShift.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}