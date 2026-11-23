package com.company.vzvod.annual;

import com.company.vzvod.annual.AnnualRules.Snap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnualRulesTest {

    private static final LocalDate BIRTH_1980 = LocalDate.of(1980, 6, 1);
    private static final LocalDate BIRTH_2000 = LocalDate.of(2000, 6, 1);

    @Test
    @DisplayName("Флюорография действует включительно до той же даты через год")
    void fluorographyInclusiveYear() {
        Snap snap = new Snap(true, LocalDate.of(2023, 12, 30), LocalDate.of(2024, 12, 30));
        assertTrue(AnnualRules.counts(AnnualItemKind.FLUOROGRAPHY, snap, BIRTH_2000, LocalDate.of(2024, 1, 1)));
        assertTrue(AnnualRules.counts(AnnualItemKind.FLUOROGRAPHY, snap, BIRTH_2000, LocalDate.of(2024, 12, 30)));
        assertFalse(AnnualRules.counts(AnnualItemKind.FLUOROGRAPHY, snap, BIRTH_2000, LocalDate.of(2024, 12, 31)));
    }

    @Test
    @DisplayName("Повторная галочка флюорографии продлевает срок от даты окончания")
    void fluorographyExtendDoesNotUseToday() {
        Snap snap = new Snap(true, LocalDate.of(2023, 12, 30), LocalDate.of(2024, 12, 30));
        AnnualRules.Extension extension = AnnualRules.extend(
                AnnualItemKind.FLUOROGRAPHY, snap, BIRTH_2000, LocalDate.of(2024, 6, 15));
        assertEquals(LocalDate.of(2024, 12, 30), extension.performedOn());
        assertEquals(LocalDate.of(2025, 12, 30), extension.validUntil());
    }

    @Test
    @DisplayName("ЭКГ до 45 лет — два года, с года 45-летия — календарный год")
    void ecgSwitchesAtFortyFive() {
        Snap old = new Snap(true, LocalDate.of(2024, 3, 1), LocalDate.of(2026, 3, 1));
        assertTrue(AnnualRules.counts(AnnualItemKind.ECG, old, BIRTH_1980, LocalDate.of(2024, 6, 1)));
        assertFalse(AnnualRules.counts(AnnualItemKind.ECG, old, BIRTH_1980, LocalDate.of(2025, 1, 1)));

        LocalDate until = AnnualRules.initialValidUntil(
                AnnualItemKind.ECG, LocalDate.of(2025, 3, 1), BIRTH_1980, LocalDate.of(2025, 3, 2));
        assertEquals(LocalDate.of(2025, 12, 31), until);
        Snap fresh = new Snap(true, LocalDate.of(2025, 3, 1), until);
        assertTrue(AnnualRules.counts(AnnualItemKind.ECG, fresh, BIRTH_1980, LocalDate.of(2025, 12, 31)));
        assertFalse(AnnualRules.counts(AnnualItemKind.ECG, fresh, BIRTH_1980, LocalDate.of(2026, 1, 1)));
    }

    @Test
    @DisplayName("Продление ЭКГ после 45 добавляет год к окончанию")
    void ecgExtendOneYearAfterFortyFive() {
        Snap snap = new Snap(true, LocalDate.of(2025, 3, 1), LocalDate.of(2025, 12, 31));
        AnnualRules.Extension extension = AnnualRules.extend(
                AnnualItemKind.ECG, snap, BIRTH_1980, LocalDate.of(2025, 6, 1));
        assertEquals(LocalDate.of(2025, 12, 31), extension.performedOn());
        assertEquals(LocalDate.of(2026, 12, 31), extension.validUntil());
        Snap extended = new Snap(true, extension.performedOn(), extension.validUntil());
        assertTrue(AnnualRules.counts(AnnualItemKind.ECG, extended, BIRTH_1980, LocalDate.of(2026, 6, 1)));
    }

    @Test
    @DisplayName("Кровь считается только с датой текущего года и галочкой")
    void bloodNeedsCurrentYearDate() {
        assertFalse(AnnualRules.counts(
                AnnualItemKind.BLOOD,
                new Snap(true, LocalDate.of(2024, 12, 30), null),
                BIRTH_2000,
                LocalDate.of(2025, 1, 1)));
        assertTrue(AnnualRules.counts(
                AnnualItemKind.BLOOD,
                new Snap(true, LocalDate.of(2025, 2, 1), null),
                BIRTH_2000,
                LocalDate.of(2025, 6, 1)));
        assertFalse(AnnualRules.counts(
                AnnualItemKind.BLOOD,
                new Snap(false, LocalDate.of(2025, 2, 1), null),
                BIRTH_2000,
                LocalDate.of(2025, 6, 1)));
    }

    @Test
    @DisplayName("Подпись анализа: нет даты, срок, просрочка")
    void analysisCaption() {
        assertEquals(AnnualRules.AnalysisNote.UNKNOWN, AnnualRules.analysisCaption(
                AnnualItemKind.BLOOD, null, null, BIRTH_2000, LocalDate.of(2026, 6, 1)).note());

        AnnualRules.AnalysisCaption blood = AnnualRules.analysisCaption(
                AnnualItemKind.BLOOD, LocalDate.of(2026, 3, 2), null, BIRTH_2000, LocalDate.of(2026, 6, 1));
        assertEquals(AnnualRules.AnalysisNote.VALID, blood.note());
        assertEquals(LocalDate.of(2026, 12, 31), blood.until());

        assertEquals(AnnualRules.AnalysisNote.EXPIRED, AnnualRules.analysisCaption(
                AnnualItemKind.BLOOD, LocalDate.of(2025, 3, 2), null, BIRTH_2000, LocalDate.of(2026, 6, 1)).note());

        AnnualRules.AnalysisCaption fluoro = AnnualRules.analysisCaption(
                AnnualItemKind.FLUOROGRAPHY,
                LocalDate.of(2025, 12, 30),
                LocalDate.of(2026, 12, 30),
                BIRTH_2000,
                LocalDate.of(2026, 6, 1));
        assertEquals(AnnualRules.AnalysisNote.VALID, fluoro.note());
        assertEquals(LocalDate.of(2026, 12, 30), fluoro.until());

        assertEquals(AnnualRules.AnalysisNote.EXPIRED, AnnualRules.analysisCaption(
                AnnualItemKind.FLUOROGRAPHY,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2025, 1, 1),
                BIRTH_2000,
                LocalDate.of(2026, 6, 1)).note());
    }

    @Test
    @DisplayName("Без даты флюорография не считается пройденной")
    void fluorographyCheckNeedsDate() {
        assertFalse(AnnualRules.dateAllowsCheck(AnnualItemKind.FLUOROGRAPHY, Snap.EMPTY, BIRTH_2000, LocalDate.of(2025, 1, 1)));
        assertTrue(AnnualRules.dateAllowsCheck(
                AnnualItemKind.FLUOROGRAPHY,
                new Snap(false, LocalDate.of(2025, 1, 10), LocalDate.of(2026, 1, 10)),
                BIRTH_2000,
                LocalDate.of(2025, 6, 1)));
    }

    @Test
    @DisplayName("Гинеколог не входит в список мужчины")
    void gynecologistHiddenForMen() {
        List<AnnualItemKind> men = AnnualRules.scoped(AnnualSection.PROFOSMOTR, false);
        List<AnnualItemKind> women = AnnualRules.scoped(AnnualSection.PROFOSMOTR, true);
        assertFalse(men.contains(AnnualItemKind.GYNECOLOGIST));
        assertTrue(women.contains(AnnualItemKind.GYNECOLOGIST));
        assertEquals(10, men.size());
        assertEquals(11, women.size());
    }

    @Test
    void percentAndLines() {
        assertEquals(0, AnnualRules.percent(0, 0));
        assertEquals(50, AnnualRules.percent(1, 2));
        assertEquals("Пройден", AnnualRules.statusLine(""));
        assertEquals("Не пройден: Кровь, ЛОР", AnnualRules.statusLine("Кровь, ЛОР"));
        assertEquals("Не пройден", AnnualRules.passedLine(false));
    }

    @Test
    @DisplayName("Сначала без отделения, потом 1-е, потом 2-е; процент не ломает равные")
    void rosterOrder() {
        AnnualRosterOrder.Key none = new AnnualRosterOrder.Key(null, "D", "C", "Яковлев", 10);
        AnnualRosterOrder.Key first = new AnnualRosterOrder.Key(1, "A", "N", "Андреев", 90);
        AnnualRosterOrder.Key second = new AnnualRosterOrder.Key(2, "D", "A", "Борисов", 5);
        List<AnnualRosterOrder.Key> keys = new java.util.ArrayList<>(List.of(second, first, none));
        keys.sort(AnnualRosterOrder.comparator(AnnualSort.DEFAULT));
        assertEquals(none, keys.get(0));
        assertEquals(first, keys.get(1));
        assertEquals(second, keys.get(2));

        keys.sort(AnnualRosterOrder.comparator(AnnualSort.PERCENT_ASC));
        assertEquals(second, keys.get(0));
        assertEquals(none, keys.get(1));
    }
}
