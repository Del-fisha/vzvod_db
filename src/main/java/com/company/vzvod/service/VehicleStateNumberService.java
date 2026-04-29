package com.company.vzvod.service;

import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class VehicleStateNumberService {

    // Extracts: L  DDD  LL  DIGITS from an already cleaned string.
    private static final Pattern EXTRACT_PATTERN = Pattern.compile(
            "([\\p{L}])(\\d{3})([\\p{L}]{2})(\\d{1,3})",
            Pattern.UNICODE_CHARACTER_CLASS
    );

    /**
     * Normalizes an input like "К  5 -98_НР6 5" into "К-598-НР_65".
     * Returns null if input is null/blank.
     *
     * Validation rule: the input must contain a subsequence:
     * letter + 3 digits + 2 letters + 1..3 digits (with any separators between).
     */
    public String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        if (s.isBlank()) {
            return null;
        }

        String cleaned = keepLettersAndDigits(s);
        Matcher m = EXTRACT_PATTERN.matcher(cleaned);
        if (!m.find()) {
            throw new IllegalArgumentException("Invalid state number format");
        }

        String l1 = m.group(1).toUpperCase(Locale.ROOT);
        String ddd = m.group(2);
        String ll = m.group(3).toUpperCase(Locale.ROOT);
        String digits = m.group(4);

        return l1 + "-" + ddd + "-" + ll + "_" + digits;
    }

    public boolean isValid(String raw) {
        if (raw == null || raw.isBlank()) {
            return false;
        }
        return EXTRACT_PATTERN.matcher(keepLettersAndDigits(raw)).find();
    }

    private static String keepLettersAndDigits(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetter(c) || Character.isDigit(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}

