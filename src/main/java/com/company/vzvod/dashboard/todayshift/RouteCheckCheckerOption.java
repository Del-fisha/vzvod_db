package com.company.vzvod.dashboard.todayshift;

import java.util.UUID;

/**
 * Командир на смене CHECKING — вариант выбора проверяющего в диалоге.
 */
public record RouteCheckCheckerOption(
        UUID serviceInfoId,
        String label
) {
}
