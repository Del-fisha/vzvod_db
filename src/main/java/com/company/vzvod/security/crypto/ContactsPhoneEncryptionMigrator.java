package com.company.vzvod.security.crypto;

import com.company.vzvod.entity.Contacts;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.core.security.SystemAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContactsPhoneEncryptionMigrator {

    private static final Logger log = LoggerFactory.getLogger(ContactsPhoneEncryptionMigrator.class);

    private final DataManager dataManager;
    private final SystemAuthenticator systemAuthenticator;

    public ContactsPhoneEncryptionMigrator(DataManager dataManager, SystemAuthenticator systemAuthenticator) {
        this.dataManager = dataManager;
        this.systemAuthenticator = systemAuthenticator;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void migrateOnStartup() {
        systemAuthenticator.runWithSystem(() -> {
            List<Contacts> contacts = dataManager.load(Contacts.class).all().list();
            if (contacts.isEmpty()) {
                return;
            }

            SaveContext sc = new SaveContext();
            int touched = 0;

            for (Contacts c : contacts) {
                if (c.getPhoneNumberRaw() == null) {
                    continue;
                }
                if (c.getPhoneNumberRaw().startsWith(CryptoService.PREFIX)) {
                    continue;
                }

                // Re-save to trigger @Convert encryption. Keep normalization via setter.
                c.setPhoneNumber(c.getPhoneNumber());
                sc.saving(c);
                touched++;
            }

            if (touched > 0) {
                dataManager.save(sc);
                log.info("Contacts migration: encrypted phone for {} contact(s)", touched);
            }
        });
    }
}

