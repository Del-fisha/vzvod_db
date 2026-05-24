package com.company.vzvod.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public enum TypeOfCriminal implements EnumClass<Integer> {

    HOT_PURSUIT(1),
    IDENTIFICATION(2),
    FEDERAL_WANTED(3),
    LOCAL_SEARCH(4),
    WATCH_LIST(5);

    private final Integer id;

    TypeOfCriminal(Integer id) {
        this.id = id;
    }

    @NonNull
    public Integer getId() {
        return id;
    }

    @Nullable
    public static TypeOfCriminal fromId(Integer id) {
        for (TypeOfCriminal at : TypeOfCriminal.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
