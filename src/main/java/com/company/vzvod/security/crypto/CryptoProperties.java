package com.company.vzvod.security.crypto;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.crypto")
public class CryptoProperties {

    /**
     * Base64 of 32 bytes (AES-256 key).
     * Recommended to provide via env/secret manager, not in VCS.
     */
    private String keyBase64;

    public String getKeyBase64() {
        return keyBase64;
    }

    public void setKeyBase64(String keyBase64) {
        this.keyBase64 = keyBase64;
    }
}

