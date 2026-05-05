package com.company.vzvod.dashboard.stats;

import java.util.UUID;

/**
 * Один объект сравнения (отделение или сотрудник): значение на каждый столбец графика.
 */
public record StatsSeries(
        UUID targetId,
        String label,
        double[] bucketValues
) {
}
