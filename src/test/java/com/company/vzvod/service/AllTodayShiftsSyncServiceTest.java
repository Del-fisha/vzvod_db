package com.company.vzvod.service;

import com.company.vzvod.entity.AllTodayShifts;
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
@DisplayName("Синхронизация AllTodayShifts из Shift")
class AllTodayShiftsSyncServiceTest {

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private AllTodayShiftsSyncService syncService;

    private LocalDate dayA;
    private LocalDate dayB;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
        dayA = LocalDate.of(2099, 1, 10);
        dayB = LocalDate.of(2099, 1, 11);
        cleanupTestRows();
    }

    @AfterEach
    void tearDown() {
        cleanupTestRows();
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("ensureExists создаёт уникальную запись по дате и отделению")
    void ensureExists_createsUniqueRow() {
        AllTodayShifts first = syncService.ensureExists(dayA, Dep.FIRST);
        AllTodayShifts second = syncService.ensureExists(dayA, Dep.FIRST);

        assertNotNull(first.getId());
        assertEquals(first.getId(), second.getId());
        assertEquals(dayA, first.getDate());
        assertEquals(Dep.FIRST, first.getDepartment());

        long count = dataManager.loadValue(
                        "select count(e) from AllTodayShifts e where e.date = :date and e.department = :dep",
                        Long.class)
                .parameter("date", dayA)
                .parameter("dep", Dep.FIRST.getId())
                .one();
        assertEquals(1L, count);
    }

    @Test
    @DisplayName("syncFromShifts вносит уникальные пары дата+отделение из существующих Shift")
    void syncFromShifts_importsDistinctDateDepartmentPairs() {
        createShift(dayA, Dep.FIRST, NumberOfShift._28);
        createShift(dayA, Dep.FIRST, NumberOfShift._30);
        createShift(dayA, Dep.SECOND, NumberOfShift._5);
        createShift(dayB, Dep.SECOND, NumberOfShift._6);

        // Убираем строки, которые мог создать listener при save(Shift) — проверяем именно sync.
        dataManager.load(AllTodayShifts.class)
                .query("select e from AllTodayShifts e where e.date in :dates")
                .parameter("dates", List.of(dayA, dayB))
                .list()
                .forEach(dataManager::remove);

        int created = syncService.syncFromShifts();
        assertEquals(3, created);

        List<AllTodayShifts> rows = dataManager.load(AllTodayShifts.class)
                .query("select e from AllTodayShifts e where e.date in :dates order by e.date, e.department")
                .parameter("dates", List.of(dayA, dayB))
                .list();

        assertEquals(3, rows.size());
        assertEquals(dayA, rows.get(0).getDate());
        assertEquals(Dep.FIRST, rows.get(0).getDepartment());
        assertEquals(dayA, rows.get(1).getDate());
        assertEquals(Dep.SECOND, rows.get(1).getDepartment());
        assertEquals(dayB, rows.get(2).getDate());
        assertEquals(Dep.SECOND, rows.get(2).getDepartment());
    }

    @Test
    @DisplayName("ensureExists игнорирует null дату или отделение")
    void ensureExists_ignoresNulls() {
        assertNull(syncService.ensureExists(null, Dep.FIRST));
        assertNull(syncService.ensureExists(dayA, null));
    }

    private void createShift(LocalDate date, Dep department, NumberOfShift number) {
        Shift shift = dataManager.create(Shift.class);
        shift.setDate(date);
        shift.setDepartmentToday(department);
        shift.setNumber(number);
        shift.setTypeOfShift(TypeOfShift.VZVOD_ROUTE);
        shift.setStartTime(LocalTime.of(9, 0));
        shift.setEndTime(LocalTime.of(21, 0));
        dataManager.save(shift);
    }

    private void cleanupTestRows() {
        List<Shift> shifts = dataManager.load(Shift.class)
                .query("select e from Shift e where e.date in :dates")
                .parameter("dates", List.of(dayA, dayB))
                .list();
        shifts.forEach(s -> dataManager.remove(s));

        List<AllTodayShifts> days = dataManager.load(AllTodayShifts.class)
                .query("select e from AllTodayShifts e where e.date in :dates")
                .parameter("dates", List.of(dayA, dayB))
                .list();
        days.forEach(d -> dataManager.remove(d));
    }
}
