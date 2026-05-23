package com.company.vzvod.service;

import com.company.vzvod.entity.Penalty;
import com.company.vzvod.entity.PenaltyStatus;
import io.jmix.core.UnconstrainedDataManager;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PenaltyExpirationService {

    private final UnconstrainedDataManager dataManager;

    public PenaltyExpirationService(UnconstrainedDataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Applies {@link Penalty#autoCompleteIfExpired(LocalDate)} to each penalty and returns changed ones.
     */
    public List<Penalty> applyExpiration(List<Penalty> penalties, LocalDate asOf) {
        List<Penalty> changed = new ArrayList<>();
        if (penalties == null || penalties.isEmpty()) {
            return changed;
        }
        LocalDate now = asOf != null ? asOf : LocalDate.now();
        for (Penalty penalty : penalties) {
            if (penalty != null && penalty.autoCompleteIfExpired(now)) {
                changed.add(penalty);
            }
        }
        return changed;
    }

    public void saveChanged(List<Penalty> penalties, LocalDate asOf) {
        List<Penalty> changed = applyExpiration(penalties, asOf);
        if (!changed.isEmpty()) {
            dataManager.save(changed);
        }
    }

    /**
     * Mass update in DB: ACTIVE penalties whose date is at least one year before {@code asOf}.
     */
    public void completeAllExpired(LocalDate asOf) {
        if (asOf == null) {
            return;
        }

        LocalDate cutoff = asOf.minusYears(1);
        List<Penalty> expired = dataManager.load(Penalty.class)
                .query("select p from Penalty p " +
                        "where p.penaltyStatus = :active " +
                        "  and p.date is not null " +
                        "  and p.date <= :cutoff")
                .parameter("active", PenaltyStatus.ACTIVE.getId())
                .parameter("cutoff", cutoff)
                .fetchPlan(fp -> fp.add("penaltyStatus").add("date"))
                .list();

        for (Penalty penalty : expired) {
            penalty.setPenaltyStatus(PenaltyStatus.COMPLETED);
        }

        if (!expired.isEmpty()) {
            dataManager.save(expired);
        }
    }
}
