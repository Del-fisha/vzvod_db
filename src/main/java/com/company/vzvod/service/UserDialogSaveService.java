package com.company.vzvod.service;

import com.company.vzvod.entity.Contacts;
import com.company.vzvod.entity.Education;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserDialogSaveService {

    private final DataManager dataManager;

    public UserDialogSaveService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * "Upsert" for User from UI detail view:
     * - if the given id already exists in DB, do UPDATE (never INSERT -> avoids USER__pkey)
     * - otherwise do normal INSERT
     *
     * We intentionally copy fields onto the managed/persisted instance to guarantee UPDATE semantics.
     */
    @Transactional
    public User saveFromDialog(User edited) {
        if (edited == null) {
            return null;
        }

        UUID id = edited.getId();
        if (id != null) {
            User persisted = dataManager.load(User.class).id(id).optional().orElse(null);
            if (persisted != null) {
                applyEditableFields(edited, persisted);
                return dataManager.save(persisted);
            }
        }

        return dataManager.save(edited);
    }

    private static void applyEditableFields(User from, User to) {
        to.setUsername(from.getUsername());
        to.setPassword(from.getPassword());
        to.setFirstName(from.getFirstName());
        to.setLastName(from.getLastName());
        to.setPatronymic(from.getPatronymic());
        to.setDateOfBirth(from.getDateOfBirth());
        to.setGender(from.getGender());
        to.setArmyService(from.getArmyService());

        Education education = from.getEducation();
        if (education != null) {
            to.setEducation(education);
        }

        Contacts contacts = from.getContactsInfo();
        if (contacts != null) {
            to.setContactsInfo(contacts);
        }

        ServiceInfo serviceInfo = from.getServiceInfo();
        if (serviceInfo != null) {
            to.setServiceInfo(serviceInfo);
        }
    }
}

