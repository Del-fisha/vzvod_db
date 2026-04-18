package com.company.vzvod.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum ArmyService implements EnumClass<String> {

    SERVED("Y"),      // СЛУЖИЛ
    NOT_SERVED("N");  // НЕ СЛУЖИЛ

    private final String id;

    ArmyService(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static ArmyService fromId(String id) {
        for (ArmyService value : ArmyService.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}