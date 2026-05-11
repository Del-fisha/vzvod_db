package com.company.vzvod.security.crypto;

import com.company.vzvod.entity.Address;
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
public class AddressEncryptionMigrator {

    private static final Logger log = LoggerFactory.getLogger(AddressEncryptionMigrator.class);

    private final DataManager dataManager;
    private final SystemAuthenticator systemAuthenticator;

    public AddressEncryptionMigrator(DataManager dataManager, SystemAuthenticator systemAuthenticator) {
        this.dataManager = dataManager;
        this.systemAuthenticator = systemAuthenticator;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void migrateOnStartup() {
        systemAuthenticator.runWithSystem(() -> {
            List<Address> addresses = dataManager.load(Address.class).all().list();
            if (addresses.isEmpty()) {
                return;
            }

            SaveContext sc = new SaveContext();
            int touched = 0;

            for (Address a : addresses) {
                boolean changed = false;

                if (a.getIndexRaw() != null && !a.getIndexRaw().startsWith(CryptoService.PREFIX)) {
                    a.setIndex(a.getIndex());
                    changed = true;
                }
                if (a.getCityRaw() != null && !a.getCityRaw().startsWith(CryptoService.PREFIX)) {
                    a.setCity(a.getCity());
                    changed = true;
                }
                if (a.getStreetRaw() != null && !a.getStreetRaw().startsWith(CryptoService.PREFIX)) {
                    a.setStreet(a.getStreet());
                    changed = true;
                }
                if (a.getHouseNumberRaw() != null && !a.getHouseNumberRaw().startsWith(CryptoService.PREFIX)) {
                    a.setHouseNumber(a.getHouseNumber());
                    changed = true;
                }
                if (a.getBodyRaw() != null && !a.getBodyRaw().startsWith(CryptoService.PREFIX)) {
                    a.setBody(a.getBody());
                    changed = true;
                }
                if (a.getFlatRaw() != null && !a.getFlatRaw().startsWith(CryptoService.PREFIX)) {
                    a.setFlat(a.getFlat());
                    changed = true;
                }

                if (changed) {
                    sc.saving(a);
                    touched++;
                }
            }

            if (touched > 0) {
                dataManager.save(sc);
                log.info("Address migration: encrypted {} address(es)", touched);
            }
        });
    }
}

