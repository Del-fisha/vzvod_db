package com.company.vzvod.bot;

import java.util.List;
import java.util.Locale;

final class StaffDirectoryPages {

    static final int DEFAULT_PAGE_SIZE = 20;
    static final int MAX_PAGE_SIZE = 50;

    private StaffDirectoryPages() {
    }

    static int normalizePage(int page) {
        return Math.max(page, 0);
    }

    static int normalizePageSize(int pageSize) {
        if (pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    static boolean matches(String displayName, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        if (displayName == null || displayName.isBlank()) {
            return false;
        }
        Locale ru = Locale.forLanguageTag("ru");
        return displayName.toLowerCase(ru).contains(query.trim().toLowerCase(ru));
    }

    static <T> List<T> slice(List<T> items, int page, int pageSize) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        int from = page * pageSize;
        if (from >= items.size()) {
            return List.of();
        }
        return List.copyOf(items.subList(from, Math.min(from + pageSize, items.size())));
    }

    static boolean hasMore(int total, int page, int pageSize) {
        return (long) (page + 1) * pageSize < total;
    }
}
