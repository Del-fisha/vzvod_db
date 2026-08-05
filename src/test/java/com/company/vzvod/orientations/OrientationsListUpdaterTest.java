package com.company.vzvod.orientations;

import com.company.vzvod.orientations.dto.OrientationDto;
import com.company.vzvod.orientations.dto.OrientationImageDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Обновление списка ориентировок")
class OrientationsListUpdaterTest {

    @Test
    @DisplayName("не меняет индекс, если список не изменился")
    void keepsIndexWhenListUnchanged() {
        OrientationDto first = dto("a.docx", "Первый", "img1");
        OrientationDto second = dto("b.docx", "Второй", "img2");
        List<OrientationDto> previous = List.of(first, second);

        OrientationsListUpdater.UpdateResult update = OrientationsListUpdater.merge(previous, 1, List.of(first, second));

        assertFalse(update.changed());
        assertEquals(1, update.currentIndex());
    }

    @Test
    @DisplayName("сохраняет текущую ориентировку после фонового обновления")
    void keepsCurrentOrientationAfterRefresh() {
        OrientationDto first = dto("a.docx (1)", "Первый", "img1");
        OrientationDto second = dto("a.docx (2)", "Второй", "img2");
        OrientationDto secondUpdated = dto("a.docx (2)", "Второй", "img2-new");

        OrientationsListUpdater.UpdateResult update = OrientationsListUpdater.merge(
                List.of(first, second),
                1,
                List.of(first, secondUpdated)
        );

        assertTrue(update.changed());
        assertEquals(1, update.currentIndex());
        assertEquals("Второй", update.orientations().get(1).text());
    }

    @Test
    @DisplayName("не сбрасывает индекс на ноль без необходимости")
    void doesNotResetToFirstWhenPossible() {
        OrientationDto first = dto("a.docx (1)", "Первый", "img1");
        OrientationDto second = dto("a.docx (2)", "Второй", "img2");
        OrientationDto third = dto("c.docx", "Третий", "img3");

        OrientationsListUpdater.UpdateResult update = OrientationsListUpdater.merge(
                List.of(first, second),
                1,
                List.of(first, second, third)
        );

        assertTrue(update.changed());
        assertEquals(1, update.currentIndex());
    }

    private static OrientationDto dto(String fileName, String text, String image) {
        return new OrientationDto(fileName, text, List.of(new OrientationImageDto(image, "image/png")));
    }
}
