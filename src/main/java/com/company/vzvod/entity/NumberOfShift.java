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
    ANOTHER("Другое");

    private final String id;

    NumberOfShift(String id) {
        this.id = id;
    }

    @Override
    @NonNull
    public String getId() {
        return id;
    }

    /**
     * Тип смены по умолчанию для выбранного маршрута (можно изменить вручную в форме).
     */
    @Nullable
    public TypeOfShift defaultTypeOfShift() {
        return switch (this) {
            case _28, _30, _31, _32 -> TypeOfShift.VZVOD_ROUTE;
            case _5, _6, _3, _18 -> TypeOfShift.BAT_POST;
            case ANOTHER -> TypeOfShift.CHECKING;
        };
    }

    @Nullable
    public static NumberOfShift fromId(String id) {
        if (id == null) {
            return null;
        }
        if ("Another".equals(id)) {
            return ANOTHER;
        }
        for (NumberOfShift v : NumberOfShift.values()) {
            if (v.getId().equals(id)) {
                return v;
            }
        }
        return null;
    }
}