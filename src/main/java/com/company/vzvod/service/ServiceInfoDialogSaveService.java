package com.company.vzvod.service;

import com.company.vzvod.entity.IdCard;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ServiceInfoDialogSaveService {

    private final DataManager dataManager;
    private final EntityStates entityStates;

    public ServiceInfoDialogSaveService(DataManager dataManager, EntityStates entityStates) {
        this.dataManager = dataManager;
        this.entityStates = entityStates;
    }

    /**
     * Saves a ServiceInfo from its dialog without causing:
     * - "relationship ... not marked cascade PERSIST" when User is still new
     * - duplicate composition inserts (PK_ID_CARD) when IdCard already exists in DB
     */
    @Transactional
    public ServiceInfo saveFromDialog(ServiceInfo edited) {
        if (edited == null) {
            return null;
        }

        // ServiceInfoDetailView dialog does not edit vacation counters, but if the instance was loaded
        // without these attributes, Java defaults (40/40) may overwrite DB values on save.
        // Preserve the persisted counters here; the counters are recalculated when Vocation is saved.
        if (edited.getId() != null) {
            Integer entitled = dataManager.loadValue(
                            "select si.vacationDaysEntitled from ServiceInfo si where si.id = :id",
                            Integer.class
                    )
                    .parameter("id", edited.getId())
                    .optional()
                    .orElse(null);
            Integer available = dataManager.loadValue(
                            "select si.vacationDaysAvailable from ServiceInfo si where si.id = :id",
                            Integer.class
                    )
                    .parameter("id", edited.getId())
                    .optional()
                    .orElse(null);

            if (entitled != null) {
                edited.setVacationDaysEntitled(entitled);
            }
            if (available != null) {
                edited.setVacationDaysAvailable(available);
            }
        }

        IdCard idCard = edited.getIdCard();
        UUID idCardId = idCard != null ? idCard.getId() : null;
        if (idCard != null) {
            // We don't cascade PERSIST from ServiceInfo.idCard anymore.
            // IdCard must exist in DB before we persist ServiceInfo, otherwise FK insert will fail.
            if (idCardId == null) {
                IdCard saved = dataManager.save(idCard);
                edited.setIdCard(saved);
            } else {
                IdCard persistedIdCard = dataManager.load(IdCard.class)
                        .id(idCardId)
                        .optional()
                        .orElse(null);
                if (persistedIdCard == null) {
                    IdCard saved = dataManager.save(idCard);
                    edited.setIdCard(saved);
                } else {
                    edited.setIdCard(persistedIdCard);
                }
            }
        }

        User user = edited.getUser();
        if (user != null && entityStates.isNew(user)) {
            // ServiceInfo.user has no cascade PERSIST, so persist User first.
            User savedUser = dataManager.save(user);

            // At this point ServiceInfo is expected to be saved via cascade from User.
            ServiceInfo persistedServiceInfo = dataManager.load(ServiceInfo.class)
                    .query("select si from ServiceInfo si where si.user.id = :uid")
                    .parameter("uid", savedUser.getId())
                    .one();
            return persistedServiceInfo;
        }

        // Normal case: User already exists, we can save ServiceInfo directly.
        return dataManager.save(edited);
    }
}

