package com.company.vzvod.annual;

import org.springframework.lang.Nullable;

public enum AnnualSort {
    DEFAULT,
    PERCENT_ASC,
    PERCENT_DESC;

    @Nullable
    public static AnnualSort fromId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        for (AnnualSort sort : values()) {
            if (sort.name().equalsIgnoreCase(id)) {
                return sort;
            }
        }
        return null;
    }
}
