package com.company.vzvod.annual;

import com.company.vzvod.view.annual.AnnualScreens;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnnualScreenTextTest {

    @Test
    void termCellIsOneCompactStatus() {
        assertEquals("Не пройден", AnnualScreens.termText(false, null));
        assertEquals("Пройден", AnnualScreens.termText(true, null));
        assertEquals("до 30.12.2026", AnnualScreens.termText(true, LocalDate.of(2026, 12, 30)));
    }

    @Test
    void medicalLineUsesPassedWords() {
        assertEquals("Профосмотр: Пройден", AnnualScreens.medicalLine(true));
        assertEquals("Профосмотр: Не пройден", AnnualScreens.medicalLine(false));
    }
}
