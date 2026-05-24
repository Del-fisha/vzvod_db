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
    private final EducationStatusService educationStatusService;

    public UserDialogSaveService(DataManager dataManager,
                                 EducationStatusService educationStatusService) {
        this.dataManager = dataManager;
        this.educationStatusService = educationStatusService;
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

        Education education = edited.getEducation();
        if (education != null) {
            educationStatusService.applyStatusFromUntil(education);
        }
        return dataManager.save(edited);
    }

    private void applyEditableFields(User from, User to) {
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
            educationStatusService.applyStatusFromUntil(education);
            Education persistedEducation = to.getEducation();
            if (persistedEducation != null
                    && education.getId() != null
                    && education.getId().equals(persistedEducation.getId())) {
                applyEducationFields(education, persistedEducation);
            } else {
                to.setEducation(education);
            }
        }

        Contacts contacts = from.getContactsInfo();
        if (contacts != null) {
            to.setContactsInfo(contacts);
        }

        ServiceInfo serviceInfo = from.getServiceInfo();
        if (serviceInfo != null && shouldApplyServiceInfo(to, serviceInfo)) {
            to.setServiceInfo(serviceInfo);
        }
    }

    /**
     * Never replace an existing persisted ServiceInfo with another instance (especially a new one
     * created in UI when {@code user.getServiceInfo()} was null). That leaves the old row orphaned
     * with penalties/vocations while the User points at an empty ServiceInfo.
     */
    private boolean shouldApplyServiceInfo(User to, ServiceInfo candidate) {
        ServiceInfo existing = to.getServiceInfo();
        if (existing == null) {
            return true;
        }
        UUID existingId = existing.getId();
        UUID candidateId = candidate.getId();
        if (existingId == null) {
            return true;
        }
        if (candidateId == null) {
            return false;
        }
        return existingId.equals(candidateId);
    }

    private static void applyEducationFields(Education from, Education to) {
        to.setStarted(from.getStarted());
        to.setUntil(from.getUntil());
        to.setType(from.getType());
        to.setStatus(from.getStatus());
        to.setNameOfInstitution(from.getNameOfInstitution());
    }
}

