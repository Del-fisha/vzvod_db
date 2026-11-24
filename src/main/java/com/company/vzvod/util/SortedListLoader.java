package com.company.vzvod.util;

import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Загружает весь результат запроса, сортирует в памяти и отдаёт страницу.
 * Нужно там, где ключ сортировки — расшифрованная фамилия или поле связанной смены:
 * {@code ORDER BY} в JPQL для таких списков либо неверен, либо ломает {@code DISTINCT} в PostgreSQL.
 */
public final class SortedListLoader {

    private SortedListLoader() {
    }

    public static <E> List<E> load(DataManager dataManager,
                                    LoadContext<E> loadContext,
                                    Comparator<? super E> comparator) {
        int first = 0;
        int max = 0;
        LoadContext.Query query = loadContext.getQuery();
        if (query != null) {
            first = Math.max(0, query.getFirstResult());
            max = query.getMaxResults();
            query.setFirstResult(0);
            query.setMaxResults(0);
        }
        List<E> all = new ArrayList<>(dataManager.loadList(loadContext));
        all.sort(comparator);
        return page(all, first, max);
    }

    static <E> List<E> page(List<E> all, int first, int max) {
        if (all.isEmpty() || first >= all.size()) {
            return List.of();
        }
        int from = Math.max(first, 0);
        int to = max <= 0 ? all.size() : Math.min(all.size(), from + max);
        if (from >= to) {
            return List.of();
        }
        return new ArrayList<>(all.subList(from, to));
    }
}
