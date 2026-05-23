package com.company.vzvod.service;

import com.company.vzvod.entity.Education;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class EducationDialogSaveService {

    private final DataManager dataManager;
    private final EducationStatusService educationStatusService;

    public EducationDialogSaveService(DataManager dataManager,
                                      EducationStatusService educationStatusService) {
        this.dataManager = dataManager;
        this.educationStatusService = educationStatusService;
    }

    @Transactional
    public Education saveFromDialog(Education edited) {
        if (edited == null) {
            return null;
        }

        educationStatusService.applyStatusFromUntil(edited);

        UUID id = edited.getId();
        if (id != null) {
            Education persisted = dataManager.load(Education.class).id(id).optional().orElse(null);
            if (persisted != null) {
                applyEditableFields(edited, persisted);
                return dataManager.save(persisted);
            }
        }

        return dataManager.save(edited);
    }

    private static void applyEditableFields(Education from, Education to) {
        to.setStarted(from.getStarted());
        to.setUntil(from.getUntil());
        to.setType(from.getType());
        to.setStatus(from.getStatus());
        to.setNameOfInstitution(from.getNameOfInstitution());
    }
}
