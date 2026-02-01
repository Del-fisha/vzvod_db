package com.company.vzvod.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;


public enum NumberOfShift implements EnumClass<String> {

    _28("МП 28"),
    _30("МП 30"),
    _31("МП 31"),
    _32("МП 32"),
    _5("МП 5"),
    _6("МП 6"),
    _3("СП 3"),
    _18("СП 18"),
    ANOTHER("Another");

    private final String id;

    NumberOfShift(String id) {
        this.id = id;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @Nullable
    public static NumberOfShift fromId(Integer id) {
        return null;
    }
}