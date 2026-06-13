package com.company.vzvod.view.shift;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Shift.detail: удаление сотрудника не должно использовать list_remove")
class ShiftDetailViewLayoutTest {

    @Test
    @DisplayName("unitsDataGrid.remove — list_exclude, не list_remove")
    void unitsGridRemoveAction_isListExclude() throws IOException {
        String xml = readResource("/com/company/vzvod/view/shift/shift-detail-view.xml");

        int unitsGridPos = xml.indexOf("id=\"unitsDataGrid\"");
        assertTrue(unitsGridPos >= 0, "unitsDataGrid not found");

        int unitsActionsPos = xml.indexOf("<actions>", unitsGridPos);
        assertTrue(unitsActionsPos >= 0, "units grid actions not found");

        int nextGridPos = xml.indexOf("<dataGrid", unitsGridPos + 1);
        String unitsActionsBlock = nextGridPos > 0
                ? xml.substring(unitsActionsPos, nextGridPos)
                : xml.substring(unitsActionsPos, unitsActionsPos + 500);

        assertTrue(unitsActionsBlock.contains("type=\"list_exclude\""),
                "units remove must use list_exclude (detach M2M only)");
        assertFalse(unitsActionsBlock.contains("type=\"list_remove\""),
                "units remove must not use list_remove (deletes ServiceInfo from DB)");
    }

    private static String readResource(String path) throws IOException {
        try (var is = ShiftDetailViewLayoutTest.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new IOException("Resource not found: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
