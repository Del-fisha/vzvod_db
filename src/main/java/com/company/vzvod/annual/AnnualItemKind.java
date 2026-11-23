package com.company.vzvod.annual;

import org.springframework.lang.Nullable;

/**
 * Пункты раздела «Ежегодно». Год хранения 0 — для флюорографии и ЭКГ:
 * их срок живёт дольше календарного года.
 */
public enum AnnualItemKind {
    LOR("ЛОР", Group.DOCTOR),
    OCULIST("Окулист", Group.DOCTOR),
    DENTIST("Стоматолог", Group.DOCTOR),
    NEUROLOGIST("Невролог", Group.DOCTOR),
    PSYCHIATRIST("Психиатр", Group.DOCTOR),
    SURGEON("Хирург", Group.DOCTOR),
    GYNECOLOGIST("Гинеколог", Group.DOCTOR),
    THERAPIST("Терапевт", Group.DOCTOR),
    BLOOD("Кровь", Group.ANALYSIS),
    FLUOROGRAPHY("Флюорография", Group.ANALYSIS),
    ECG("ЭКГ", Group.ANALYSIS),
    CPD("ЦПД", Group.CPD),
    SHOOTING("Стрельбы", Group.TEST),
    RUNNING("Бег", Group.TEST),
    STRENGTH("Силовые упражнения", Group.TEST),
    TECHNIQUES("Приёмы", Group.TEST),
    PSYCHOLOGIST("Психолог", Group.PSYCHOLOGIST);

    public static final int ROLLING_YEAR = 0;

    private final String title;
    private final Group group;

    AnnualItemKind(String title, Group group) {
        this.title = title;
        this.group = group;
    }

    public String title() {
        return title;
    }

    public Group group() {
        return group;
    }

    public boolean appliesTo(boolean female) {
        return this != GYNECOLOGIST || female;
    }

    public boolean profosmotr() {
        return group == Group.DOCTOR || group == Group.ANALYSIS;
    }

    public boolean rollingAnalysis() {
        return this == FLUOROGRAPHY || this == ECG;
    }

    public boolean dateAllowed() {
        return this == BLOOD || this == FLUOROGRAPHY || this == ECG || this == CPD;
    }

    public int storageYear(int calendarYear) {
        return rollingAnalysis() ? ROLLING_YEAR : calendarYear;
    }

    @Nullable
    public static AnnualItemKind fromId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        for (AnnualItemKind kind : values()) {
            if (kind.name().equals(id)) {
                return kind;
            }
        }
        return null;
    }

    public enum Group {
        DOCTOR, ANALYSIS, CPD, TEST, PSYCHOLOGIST
    }
}
