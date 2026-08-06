package com.company.vzvod.mobile.dto;

import java.util.UUID;

public record MobileAuthResponse(
        UUID userId,
        String displayName,
        String username,
        /** FULL_ACCESS или POLICEMAN */
        String role,
        boolean fullAccess,
        String token
) {
}
