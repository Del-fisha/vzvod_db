package com.company.vzvod.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Compatibility encoder used only for matching passwords that have no "{id}" prefix in DB.
 * <p>
 * - If the stored value looks like a BCrypt hash (starts with "$2"), we match with BCrypt.
 * - Otherwise, we treat it as a plaintext legacy value (noop).
 * <p>
 * Encoding is NOT used; DelegatingPasswordEncoder still encodes using its configured id (bcrypt).
 */
public final class LegacyPasswordEncoderForMatches implements PasswordEncoder {

    private final PasswordEncoder bcrypt = new BCryptPasswordEncoder();
    private final PasswordEncoder noop = NoOpPasswordEncoder.getInstance();

    @Override
    public String encode(CharSequence rawPassword) {
        // Not used in our flow; we only plug this into defaultPasswordEncoderForMatches.
        return bcrypt.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null) {
            return false;
        }
        if (encodedPassword.startsWith("$2")) {
            return bcrypt.matches(rawPassword, encodedPassword);
        }
        return noop.matches(rawPassword, encodedPassword);
    }
}

