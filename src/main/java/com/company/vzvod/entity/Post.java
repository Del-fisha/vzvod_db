package com.company.vzvod.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum Post implements EnumClass<String> {

    COM_VZVOD("A"),
    ZAM_COM_VZVOD("B"),
    COM_OTD("C"),
    POLICEMAN("D"),
    INTERN("E");

    private final String id;

    Post(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static Post fromId(String id) {
        for (Post at : Post.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}