package com.company.vzvod.mobile.dto;

public record MobileAuthRequest(
        String username,
        String password,
        String deviceId
) {
}
