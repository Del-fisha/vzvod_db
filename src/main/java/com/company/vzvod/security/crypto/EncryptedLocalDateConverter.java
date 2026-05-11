package com.company.vzvod.security.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDate;

@Converter
public class EncryptedLocalDateConverter implements AttributeConverter<LocalDate, String> {

    @Override
    public String convertToDatabaseColumn(LocalDate attribute) {
        if (attribute == null) {
            return null;
        }
        // ISO-8601 (yyyy-MM-dd) is stable and locale-free.
        return SpringContext.getBean(CryptoService.class).encryptToString(attribute.toString());
    }

    @Override
    public LocalDate convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        String plaintext = SpringContext.getBean(CryptoService.class).decryptFromString(dbData);
        return plaintext == null || plaintext.isBlank() ? null : LocalDate.parse(plaintext);
    }
}

