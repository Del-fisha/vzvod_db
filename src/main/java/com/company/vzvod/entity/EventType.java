package com.company.vzvod.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum EventType implements EnumClass<String> {

    SPORT("SPORT"),
    CONCERT("CONCERT"),
    FORUM("FORUM"),
    OPM("MIGRANT"),
    OTHER("OTHER");

    private final String id;

    EventType(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static EventType fromId(@Nullable String id) {
        if (id == null) {
            return null;
        }
        for (EventType t : EventType.values()) {
            if (t.getId().equals(id)) {
                return t;
            }
        }
        return null;
    }
}