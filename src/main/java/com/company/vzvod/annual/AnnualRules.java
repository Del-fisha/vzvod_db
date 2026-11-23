package com.company.vzvod.annual;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Сроки анализов и признак «считается сданным» на конкретную дату.
 * Галочка нигде не подставляет сегодняшний день.
 */
public final class AnnualRules {

    public static final ZoneId MOSCOW = ZoneId.of("Europe/Moscow");

    private AnnualRules() {
    }

    public static LocalDate todayMoscow() {
        return LocalDate.now(MOSCOW);
    }

    public record Snap(boolean checked, LocalDate performedOn, LocalDate validUntil) {
        public static final Snap EMPTY = new Snap(false, null, null);
    }

    public record ItemState(
            AnnualItemKind kind,
            boolean checked,
            boolean counts,
            boolean extendable,
            LocalDate performedOn,
            LocalDate validUntil
    ) {
    }

    public record Extension(LocalDate performedOn, LocalDate validUntil) {
    }

    public static boolean ecgAnnual(LocalDate birth, LocalDate today) {
        return birth != null && today.getYear() >= birth.getYear() + 45;
    }

    public static LocalDate initialValidUntil(AnnualItemKind kind, LocalDate performed, LocalDate birth, LocalDate today) {
        if (performed == null) {
            return null;
        }
        if (kind == AnnualItemKind.FLUOROGRAPHY) {
            return performed.plusYears(1);
        }
        if (kind == AnnualItemKind.ECG) {
            if (ecgAnnual(birth, today)) {
                return LocalDate.of(performed.getYear(), 12, 31);
            }
            return performed.plusYears(2);
        }
        return null;
    }

    public static ItemState view(AnnualItemKind kind, Snap snap, LocalDate birth, LocalDate today) {
        Snap safe = snap == null ? Snap.EMPTY : snap;
        boolean counts = counts(kind, safe, birth, today);
        boolean extendable = kind.rollingAnalysis() && counts;
        return new ItemState(kind, safe.checked(), counts, extendable, safe.performedOn(), safe.validUntil());
    }

    public static boolean counts(AnnualItemKind kind, Snap snap, LocalDate birth, LocalDate today) {
        if (snap == null || !snap.checked()) {
            return false;
        }
        return switch (kind) {
            case BLOOD -> snap.performedOn() != null && snap.performedOn().getYear() == today.getYear();
            case FLUOROGRAPHY -> covers(fluoroUntil(snap), today);
            case ECG -> ecgCounts(snap, birth, today);
            default -> true;
        };
    }

    public static boolean dateAllowsCheck(AnnualItemKind kind, Snap snap, LocalDate birth, LocalDate today) {
        if (!kind.rollingAnalysis()) {
            return true;
        }
        Snap asChecked = new Snap(true, snap.performedOn(), snap.validUntil());
        return counts(kind, asChecked, birth, today);
    }

    public static Extension extend(AnnualItemKind kind, Snap snap, LocalDate birth, LocalDate today) {
        LocalDate currentEnd = switch (kind) {
            case FLUOROGRAPHY -> fluoroUntil(snap);
            case ECG -> ecgUntil(snap, birth, today);
            default -> null;
        };
        if (currentEnd == null) {
            throw new IllegalStateException("no validity to extend");
        }
        int years = kind == AnnualItemKind.ECG && !ecgAnnual(birth, today) ? 2 : 1;
        return new Extension(currentEnd, currentEnd.plusYears(years));
    }

    public static int percent(int done, int total) {
        if (total <= 0) {
            return 0;
        }
        return done * 100 / total;
    }

    public static String statusLine(String missingJoined) {
        if (missingJoined == null || missingJoined.isBlank()) {
            return "Пройден";
        }
        return "Не пройден: " + missingJoined;
    }

    public static String passedLine(boolean passed) {
        return passed ? "Пройден" : "Не пройден";
    }

    public static List<AnnualItemKind> scoped(AnnualSection section, boolean female) {
        List<AnnualItemKind> all = new ArrayList<>();
        for (AnnualItemKind kind : AnnualItemKind.values()) {
            if (kind.appliesTo(female) && section.includes(kind)) {
                all.add(kind);
            }
        }
        return all;
    }

    private static boolean ecgCounts(Snap snap, LocalDate birth, LocalDate today) {
        if (snap.performedOn() == null && snap.validUntil() == null) {
            return false;
        }
        if (ecgAnnual(birth, today)) {
            if (snap.performedOn() != null && snap.performedOn().getYear() == today.getYear()) {
                return true;
            }
            LocalDate until = snap.validUntil();
            if (until == null || snap.performedOn() == null) {
                return false;
            }
            boolean twoYearCertificate = snap.performedOn().plusYears(1).isBefore(until);
            if (twoYearCertificate) {
                return false;
            }
            return covers(until, today);
        }
        return covers(ecgUntil(snap, birth, today), today);
    }

    private static LocalDate fluoroUntil(Snap snap) {
        if (snap.validUntil() != null) {
            return snap.validUntil();
        }
        return snap.performedOn() == null ? null : snap.performedOn().plusYears(1);
    }

    private static LocalDate ecgUntil(Snap snap, LocalDate birth, LocalDate today) {
        if (snap.validUntil() != null) {
            return snap.validUntil();
        }
        return initialValidUntil(AnnualItemKind.ECG, snap.performedOn(), birth, today);
    }

    private static boolean covers(LocalDate until, LocalDate today) {
        return until != null && !today.isAfter(until);
    }
}
