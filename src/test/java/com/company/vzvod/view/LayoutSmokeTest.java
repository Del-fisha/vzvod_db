package com.company.vzvod.view;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LayoutSmokeTest {

    @Test
    void userCardView_hasVacationEntitledUsedAvailableFields() throws IOException {
        String xml = readResource("/com/company/vzvod/view/usercard/user-card-view.xml");
        assertTrue(xml.contains("id=\"vacationDaysEntitledField\""));
        assertTrue(xml.contains("id=\"vacationDaysUsedField\""));
        assertTrue(xml.contains("id=\"vacationDaysAvailableField\""));
    }

    @Test
    void vocationDetailView_doesNotContainManualCountOfDaysField() throws IOException {
        String xml = readResource("/com/company/vzvod/view/vocation/vocation-detail-view.xml");
        assertFalse(xml.contains("id=\"countOfDaysField\""));
    }

    private static String readResource(String path) throws IOException {
        try (var is = LayoutSmokeTest.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new IOException("Resource not found: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}

