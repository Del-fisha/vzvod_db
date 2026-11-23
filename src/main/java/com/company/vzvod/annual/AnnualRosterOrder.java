package com.company.vzvod.annual;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public final class AnnualRosterOrder {

    private static final Collator RU = Collator.getInstance(Locale.forLanguageTag("ru"));

    static {
        RU.setStrength(Collator.PRIMARY);
    }

    private AnnualRosterOrder() {
    }

    public record Key(Integer departmentNumber, String postId, String rankId, String lastName, int percent) {
    }

    public static Comparator<Key> comparator(AnnualSort sort) {
        Comparator<Key> natural = Comparator
                .comparingInt((Key key) -> departmentKey(key.departmentNumber()))
                .thenComparing(Key::postId, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Key::rankId, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Key::lastName, Comparator.nullsLast(RU));
        if (sort == AnnualSort.PERCENT_ASC) {
            return Comparator.comparingInt(Key::percent).thenComparing(natural);
        }
        if (sort == AnnualSort.PERCENT_DESC) {
            return Comparator.comparingInt(Key::percent).reversed().thenComparing(natural);
        }
        return natural;
    }

    public static int departmentKey(Integer number) {
        if (number == null) {
            return 0;
        }
        if (number == 1) {
            return 1;
        }
        if (number == 2) {
            return 2;
        }
        return 10 + number;
    }

    public static String departmentTitle(Integer number) {
        if (number == null) {
            return "Без отделения";
        }
        return number + "-е отделение";
    }
}
