package com.company.vzvod.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("VocationType")
class VocationTypeTest {

    @Test
    @DisplayName("Учебный отпуск и Цпп присутствуют в enum и резолвятся по id")
    void studyLeaveAndPtc_existAndResolveById() {
        assertNotNull(VocationType.STUDY_LEAVE);
        assertNotNull(VocationType.PTC);
        assertEquals(VocationType.STUDY_LEAVE, VocationType.fromId(VocationType.STUDY_LEAVE.getId()));
        assertEquals(VocationType.PTC, VocationType.fromId(VocationType.PTC.getId()));
    }

    @Test
    @DisplayName("Id новых типов не пересекаются с существующими")
    void newTypeIds_doNotCollideWithExisting() {
        assertEquals(40, VocationType.STUDY_LEAVE.getId());
        assertEquals(50, VocationType.PTC.getId());
        assertEquals(10, VocationType.MAIN.getId());
        assertEquals(20, VocationType.ADDITIONAL.getId());
        assertEquals(30, VocationType.PART_OF_MAIN.getId());
    }
}
