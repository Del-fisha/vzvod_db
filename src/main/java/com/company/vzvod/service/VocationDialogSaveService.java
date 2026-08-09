package com.company.vzvod.service;

import com.company.vzvod.entity.Vocation;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class VocationDialogSaveService {

    private final DataManager dataManager;

    public VocationDialogSaveService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Transactional
    public Vocation saveFromDialog(Vocation edited) {
        if (edited == null) {
            return null;
        }

        UUID id = edited.getId();
        if (id != null) {
            Vocation persisted = dataManager.load(Vocation.class).id(id).optional().orElse(null);
            if (persisted != null) {
                applyEditableFields(edited, persisted);
                return dataManager.save(persisted);
            }
        }

        return dataManager.save(edited);
    }

    static void applyEditableFields(Vocation from, Vocation to) {
        to.setUserServiceInfo(from.getUserServiceInfo());
        to.setType(from.getType());
        to.setStartDate(from.getStartDate());
        to.setEndDate(from.getEndDate());
        to.setCityToDrive(from.getCityToDrive());
        to.setDaysAddedByDeparture(from.getDaysAddedByDeparture());
        to.setHasDeparture(from.isHasDeparture());
    }
}
