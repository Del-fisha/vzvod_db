package com.company.vzvod.annual;

import org.springframework.lang.Nullable;

public enum AnnualSection {
    ALL,
    PROFOSMOTR,
    TESTS,
    CPD,
    PSYCHOLOGIST;

    public boolean includes(AnnualItemKind kind) {
        return switch (this) {
            case ALL -> true;
            case PROFOSMOTR -> kind.profosmotr();
            case TESTS -> kind.group() == AnnualItemKind.Group.TEST;
            case CPD -> kind == AnnualItemKind.CPD;
            case PSYCHOLOGIST -> kind == AnnualItemKind.PSYCHOLOGIST;
        };
    }

    public String title() {
        return switch (this) {
            case ALL -> "Всё";
            case PROFOSMOTR -> "Профосмотр";
            case TESTS -> "Зачёты";
            case CPD -> "ЦПД";
            case PSYCHOLOGIST -> "Психолог";
        };
    }

    @Nullable
    public static AnnualSection fromId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        for (AnnualSection section : values()) {
            if (section.name().equalsIgnoreCase(id)) {
                return section;
            }
        }
        return null;
    }
}
