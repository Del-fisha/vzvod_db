package com.company.vzvod.security.crypto;

import com.company.vzvod.entity.User;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.core.security.SystemAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Profile("!test & !test-postgres")
public class UserPiiEncryptionMigrator {

    private static final Logger log = LoggerFactory.getLogger(UserPiiEncryptionMigrator.class);

    private final DataManager dataManager;
    private final SystemAuthenticator systemAuthenticator;
    private final JdbcTemplate jdbcTemplate;
    private final CryptoService cryptoService;

    public UserPiiEncryptionMigrator(
            DataManager dataManager,
            SystemAuthenticator systemAuthenticator,
            JdbcTemplate jdbcTemplate,
            CryptoService cryptoService
    ) {
        this.dataManager = dataManager;
        this.systemAuthenticator = systemAuthenticator;
        this.jdbcTemplate = jdbcTemplate;
        this.cryptoService = cryptoService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void migrateOnStartup() {
        systemAuthenticator.runWithSystem(() -> {
            List<User> users = dataManager.load(User.class).all().list();
            if (users.isEmpty()) {
                return;
            }

            // Legacy plaintext column is not mapped on User (avoids partial-fetch / detached errors).
            Map<UUID, LocalDate> legacyBirthByUserId = new HashMap<>();
            jdbcTemplate.query("SELECT ID, DATE_OF_BIRTH FROM USER_", rs -> {
                while (rs.next()) {
                    UUID id = rs.getObject(1, UUID.class);
                    java.sql.Date d = rs.getDate(2);
                    if (id != null && d != null) {
                        legacyBirthByUserId.put(id, d.toLocalDate());
                    }
                }
                return null;
            });

            SaveContext sc = new SaveContext();
            int touched = 0;

            for (User u : users) {
                boolean changed = false;

                // Wrong-key / corrupted ciphertext: decrypt returns null in non-prod; repair so we can re-save with current key.
                if (looksEncryptedButUndecrypted(u.getFirstNameRaw(), u.getFirstName())) {
                    log.warn("User {}: FIRST_NAME ciphertext unreadable with current key; placeholder applied. Edit user in UI.",
                            u.getId());
                    u.setFirstName("-");
                    changed = true;
                }
                if (looksEncryptedButUndecrypted(u.getLastNameRaw(), u.getLastName())) {
                    log.warn("User {}: LAST_NAME ciphertext unreadable with current key; placeholder applied. Edit user in UI.",
                            u.getId());
                    u.setLastName("-");
                    changed = true;
                }
                if (looksEncryptedButUndecrypted(u.getPatronymicRaw(), u.getPatronymic())) {
                    log.warn("User {}: PATRONYMIC ciphertext unreadable with current key; placeholder applied. Edit user in UI.",
                            u.getId());
                    u.setPatronymic("-");
                    changed = true;
                }
                if (u.getDateOfBirthEncRaw() != null && u.getDateOfBirthEncRaw().startsWith(CryptoService.PREFIX)) {
                    String decryptedIso = cryptoService.decryptFromString(u.getDateOfBirthEncRaw());
                    if (decryptedIso == null) {
                        LocalDate legacy = legacyBirthByUserId.get(u.getId());
                        if (legacy != null) {
                            log.warn("User {}: DATE_OF_BIRTH_ENC unreadable; re-filled from legacy DATE_OF_BIRTH.", u.getId());
                            u.setDateOfBirth(legacy);
                        } else {
                            log.warn("User {}: DATE_OF_BIRTH_ENC unreadable and legacy date empty; using 1950-01-01. Edit user in UI.",
                                    u.getId());
                            u.setDateOfBirth(LocalDate.of(1950, 1, 1));
                        }
                        changed = true;
                    }
                }

                // Trigger encryption-on-save for legacy plaintext names.
                if (u.getFirstNameRaw() != null && !u.getFirstNameRaw().startsWith(CryptoService.PREFIX)) {
                    u.setFirstName(u.getFirstName());
                    changed = true;
                }
                if (u.getLastNameRaw() != null && !u.getLastNameRaw().startsWith(CryptoService.PREFIX)) {
                    u.setLastName(u.getLastName());
                    changed = true;
                }
                if (u.getPatronymicRaw() != null && !u.getPatronymicRaw().startsWith(CryptoService.PREFIX)) {
                    u.setPatronymic(u.getPatronymic());
                    changed = true;
                }

                // Move legacy DATE column -> encrypted column (legacy read via JDBC, not entity field).
                if ((u.getDateOfBirthEncRaw() == null || u.getDateOfBirthEncRaw().isBlank())
                        && u.getDateOfBirth() == null
                        && legacyBirthByUserId.containsKey(u.getId())) {
                    u.setDateOfBirth(legacyBirthByUserId.get(u.getId()));
                    changed = true;
                }

                if (changed) {
                    sc.saving(u);
                    touched++;
                }
            }

            if (touched > 0) {
                dataManager.save(sc);
                log.info("PII migration: encrypted {} user(s)", touched);
            }

            // Wipe legacy plaintext DATE_OF_BIRTH only when ENC decrypts with the current key (do not erase backup if ENC is broken).
            List<User> usersAgain = dataManager.load(User.class).all().list();
            int wiped = 0;
            for (User u : usersAgain) {
                String enc = u.getDateOfBirthEncRaw();
                if (enc == null || enc.isBlank()) {
                    continue;
                }
                if (!enc.startsWith(CryptoService.PREFIX)) {
                    continue;
                }
                String pt = cryptoService.decryptFromString(enc);
                if (pt == null || pt.isBlank()) {
                    continue;
                }
                int n = jdbcTemplate.update("update USER_ set DATE_OF_BIRTH = null where ID = ?", u.getId());
                wiped += n;
            }
            if (wiped > 0) {
                log.info("PII migration: wiped legacy DATE_OF_BIRTH for {} user(s) (ENC decrypts OK)", wiped);
            }
        });
    }

    private static boolean looksEncryptedButUndecrypted(String rawDb, String decryptedField) {
        return rawDb != null
                && rawDb.startsWith(CryptoService.PREFIX)
                && (decryptedField == null || decryptedField.isBlank());
    }
}

