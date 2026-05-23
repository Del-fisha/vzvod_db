package com.company.vzvod.service;

import com.company.vzvod.entity.Education;
import com.company.vzvod.entity.EducationStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EducationStatusService {

    /**
     * If {@code until} is set: studying while today is on or before the end date,
     * finished once today is after the end date.
     */
    public void applyStatusFromUntil(Education education) {
        if (education == null) {
            return;
        }
        LocalDate until = education.getUntil();
        if (until == null) {
            return;
        }
        LocalDate today = LocalDate.now();
        if (!today.isAfter(until)) {
            education.setStatus(EducationStatus.AT_THE_MOMENT);
        } else {
            education.setStatus(EducationStatus.FINISHED);
        }
    }
}
