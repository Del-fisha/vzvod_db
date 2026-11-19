package com.company.vzvod.bot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Список сотрудников для командира")
class StaffDirectoryPagesTest {

    @Test
    @DisplayName("пустой запрос подходит всем, часть фамилии ищется без учёта регистра")
    void matchesQuery() {
        assertTrue(StaffDirectoryPages.matches("Иванов Иван Иванович", null));
        assertTrue(StaffDirectoryPages.matches("Иванов Иван Иванович", "  "));
        assertTrue(StaffDirectoryPages.matches("Иванов Иван Иванович", "иван"));
        assertFalse(StaffDirectoryPages.matches("Иванов Иван Иванович", "петров"));
        assertFalse(StaffDirectoryPages.matches(null, "иван"));
    }

    @Test
    @DisplayName("страница обрезается по размеру и сообщает, есть ли продолжение")
    void sliceAndHasMore() {
        List<String> items = List.of("a", "b", "c", "d", "e");
        assertEquals(List.of("a", "b"), StaffDirectoryPages.slice(items, 0, 2));
        assertEquals(List.of("e"), StaffDirectoryPages.slice(items, 2, 2));
        assertEquals(List.of(), StaffDirectoryPages.slice(items, 3, 2));
        assertTrue(StaffDirectoryPages.hasMore(5, 0, 2));
        assertTrue(StaffDirectoryPages.hasMore(5, 1, 2));
        assertFalse(StaffDirectoryPages.hasMore(5, 2, 2));
        assertEquals(20, StaffDirectoryPages.normalizePageSize(0));
        assertEquals(50, StaffDirectoryPages.normalizePageSize(80));
        assertEquals(0, StaffDirectoryPages.normalizePage(-3));
    }
}
