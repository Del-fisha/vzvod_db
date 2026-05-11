package com.company.vzvod.security.crypto;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Service;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CryptoService {

    private static final Logger log = LoggerFactory.getLogger(CryptoService.class);

    public static final String PREFIX = "enc:v1:";
    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_BYTES = 12;

    private final SecureRandom secureRandom = new SecureRandom();
    private final SecretKey key;
    private final boolean prodProfileActive;

    public CryptoService(CryptoProperties props, Environment environment) {
        String keyB64 = props.getKeyBase64();

        boolean prod = false;
        for (String p : environment.getActiveProfiles()) {
            if ("prod".equalsIgnoreCase(p)) {
                prod = true;
                break;
            }
        }
        this.prodProfileActive = prod;

        if (keyB64 == null || keyB64.isBlank()) {
            if (prod) {
                throw new IllegalStateException("Missing encryption key. Set app.crypto.key-base64 (32 bytes base64).");
            }

            // Dev/test convenience: generate ephemeral key so local run works without secrets.
            // WARNING: restarting the app with a new key will make previously encrypted data unreadable.
            byte[] generated = new byte[32];
            secureRandom.nextBytes(generated);
            keyB64 = Base64.getEncoder().encodeToString(generated);
            log.warn("app.crypto.key-base64 is not set. Generated ephemeral key for non-prod run. " +
                    "Persist the key to keep data decryptable across restarts.");
        }
        byte[] raw = Base64.getDecoder().decode(keyB64.trim());
        if (raw.length != 32) {
            throw new IllegalStateException("app.crypto.key-base64 must decode to 32 bytes (AES-256). Got " + raw.length);
        }
        this.key = new SecretKeySpec(raw, "AES");
    }

    public String encryptToString(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        if (plaintext.startsWith(PREFIX)) {
            return plaintext;
        }

        byte[] iv = new byte[IV_BYTES];
        secureRandom.nextBytes(iv);

        try {
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] out = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ct, 0, out, iv.length, ct.length);

            return PREFIX + Base64.getEncoder().encodeToString(out);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    public String decryptFromString(String ciphertext) {
        if (ciphertext == null) {
            return null;
        }
        if (!ciphertext.startsWith(PREFIX)) {
            // Backward compatibility / migration: value in DB is still plaintext.
            return ciphertext;
        }
        String payload = ciphertext.substring(PREFIX.length());
        final byte[] in;
        try {
            in = Base64.getDecoder().decode(payload);
        } catch (IllegalArgumentException e) {
            if (prodProfileActive) {
                throw new IllegalStateException("Invalid ciphertext (base64)", e);
            }
            log.warn("Invalid ciphertext (base64); treating as null. {}", e.getMessage());
            return null;
        }
        if (in.length < IV_BYTES + 1) {
            if (prodProfileActive) {
                throw new IllegalArgumentException("Ciphertext payload is too short");
            }
            log.warn("Ciphertext payload is too short; treating as null.");
            return null;
        }
        byte[] iv = new byte[IV_BYTES];
        byte[] ct = new byte[in.length - IV_BYTES];
        System.arraycopy(in, 0, iv, 0, IV_BYTES);
        System.arraycopy(in, IV_BYTES, ct, 0, ct.length);

        try {
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] pt = cipher.doFinal(ct);
            return new String(pt, StandardCharsets.UTF_8);
        } catch (AEADBadTagException e) {
            if (prodProfileActive) {
                throw new IllegalStateException("Decryption failed (wrong key or corrupted ciphertext)", e);
            }
            log.warn(
                    "Decryption failed (wrong key or ciphertext from another run). "
                            + "Re-save affected fields in the app or fix DB values. Treating as null."
            );
            return null;
        } catch (GeneralSecurityException e) {
            if (prodProfileActive) {
                throw new IllegalStateException("Decryption failed", e);
            }
            log.warn("Decryption failed; treating as null.", e);
            return null;
        }
    }
}

