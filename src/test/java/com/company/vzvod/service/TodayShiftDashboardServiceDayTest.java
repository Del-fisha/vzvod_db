package com.company.vzvod.service;

import com.company.vzvod.dashboard.todayshift.TodayShiftDashboardSnapshot;
import com.company.vzvod.entity.Dep;
import com.company.vzvod.entity.NumberOfShift;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.entity.TypeOfShift;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("TodayShiftDashboardService за конкретный день")
class TodayShiftDashboardServiceDayTest {

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private TodayShiftDashboardService dashboardService;

    private final LocalDate day = LocalDate.of(2097, 3, 20);

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
        cleanup();
    }

    @AfterEach
    void tearDown() {
        cleanup();
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("loadSnapshot(date, dep) возвращает маршруты только выбранного дня и отделения")
    void loadSnapshotForDay_filtersByDateAndDepartment() {
        createShift(day, Dep.FIRST, NumberOfShift._28, 0, 0, LocalTime.of(21, 0));
        createShift(day, Dep.SECOND, NumberOfShift._30, 0, 0, LocalTime.of(21, 0));
        createShift(day.plusDays(1), Dep.FIRST, NumberOfShift._31, 0, 0, LocalTime.of(21, 0));

        TodayShiftDashboardSnapshot snapshot = dashboardService.loadSnapshot(day, Dep.FIRST);

        assertEquals(day, snapshot.operationalDate());
        assertEquals(1, snapshot.departmentNumber());
        assertEquals(1, snapshot.routes().size());
        assertEquals(NumberOfShift._28.getId(), snapshot.routes().get(0).routeLabel());
    }

    @Test
    @DisplayName("KPI: сумма migrant за день как у ibdr; endTime в карточке маршрута")
    void loadSnapshotForDay_sumsMigrantAndExposesEndTime() {
        createShift(day, Dep.FIRST, NumberOfShift._28, 3, 5, null);
        createShift(day, Dep.FIRST, NumberOfShift._30, 2, 4, LocalTime.of(21, 0));

        TodayShiftDashboardSnapshot snapshot = dashboardService.loadSnapshot(day, Dep.FIRST);

        assertEquals(5, snapshot.totalIbdr());
        assertEquals(9, snapshot.totalMigrant());
        assertEquals(2, snapshot.routes().size());
        assertTrue(snapshot.routes().stream().anyMatch(r -> r.endTime() == null));
        assertTrue(snapshot.routes().stream().anyMatch(r -> LocalTime.of(21, 0).equals(r.endTime())));
    }

    private void createShift(
            LocalDate date,
            Dep department,
            NumberOfShift number,
            int ibdr,
            int migrant,
            LocalTime endTime
    ) {
        Shift shift = dataManager.create(Shift.class);
        shift.setDate(date);
        shift.setDepartmentToday(department);
        shift.setNumber(number);
        shift.setTypeOfShift(TypeOfShift.VZVOD_ROUTE);
        shift.setStartTime(LocalTime.of(9, 0));
        shift.setEndTime(endTime);
        shift.setIbdr(ibdr);
        shift.setMigrant(migrant);
        dataManager.save(shift);
    }

    private void cleanup() {
        List<Shift> shifts = dataManager.load(Shift.class)
                .query("select e from Shift e where e.date in :dates")
                .parameter("dates", List.of(day, day.plusDays(1)))
                .list();
        shifts.forEach(dataManager::remove);
    }
}
