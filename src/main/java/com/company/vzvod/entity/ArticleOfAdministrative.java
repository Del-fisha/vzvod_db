package com.company.vzvod.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;


public enum ArticleOfAdministrative implements EnumClass<Integer> {

    _18_8(0),
    _20_20(1),
    _20_21(3),
    _20_1(4),
    _20_1_2(5),
    _11_15(6),
    _19_3(7),
    _20_25(8),
    ANOTHER(9);

    private final Integer id;

    ArticleOfAdministrative(Integer id) {
        this.id = id;
    }

    @NonNull
    public Integer getId() {
        return id;
    }

    @Nullable
    public static ArticleOfAdministrative fromId(Integer id) {
        for (ArticleOfAdministrative at : ArticleOfAdministrative.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}