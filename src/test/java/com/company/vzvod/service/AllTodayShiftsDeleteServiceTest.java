package com.company.vzvod.service;

import com.company.vzvod.entity.AdministrativeViolation;
import com.company.vzvod.entity.AllTodayShifts;
import com.company.vzvod.entity.ArticleOfAdministrative;
import com.company.vzvod.entity.CriminalViolation;
import com.company.vzvod.entity.Dep;
import com.company.vzvod.entity.Impact;
import com.company.vzvod.entity.NumberOfShift;
import com.company.vzvod.entity.RouteCheck;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.entity.TypeOfCriminal;
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
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Удаление смен AllTodayShifts")
class AllTodayShiftsDeleteServiceTest {

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private AllTodayShiftsDeleteService deleteService;

    @Autowired
    private AllTodayShiftsSyncService syncService;

    private final LocalDate day = LocalDate.of(2096, 8, 15);

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
    @DisplayName("deleteShifts удаляет только выбранные смены")
    void deleteShifts_removesOnlySelected() {
        Shift keep = createShift(day, Dep.FIRST, NumberOfShift._28);
        Shift remove = createShift(day, Dep.FIRST, NumberOfShift._30);
        UUID removeId = remove.getId();

        deleteService.deleteShifts(List.of(remove));

        assertTrue(dataManager.load(Shift.class).id(keep.getId()).optional().isPresent());
        assertTrue(dataManager.load(Shift.class).id(removeId).optional().isEmpty());
    }

    @Test
    @DisplayName("deleteShifts удаляет смену вместе с административными нарушениями")
    void deleteShifts_removesLinkedAdministrativeViolations() {
        Shift shift = createShift(day, Dep.FIRST, NumberOfShift._28);
        AdministrativeViolation violation = createAdminViolation(shift);
        UUID violationId = violation.getId();
        UUID shiftId = shift.getId();

        deleteService.deleteShifts(List.of(shift));

        assertTrue(dataManager.load(Shift.class).id(shiftId).optional().isEmpty());
        assertTrue(dataManager.load(AdministrativeViolation.class).id(violationId).optional().isEmpty());
    }

    @Test
    @DisplayName("deleteShifts удаляет смену вместе с уголовными нарушениями")
    void deleteShifts_removesLinkedCriminalViolations() {
        Shift shift = createShift(day, Dep.FIRST, NumberOfShift._28);
        CriminalViolation violation = createCriminalViolation(shift);
        UUID violationId = violation.getId();

        deleteService.deleteShifts(List.of(shift));

        assertTrue(dataManager.load(CriminalViolation.class).id(violationId).optional().isEmpty());
    }

    @Test
    @DisplayName("После удаления последней смены дня строка AllTodayShifts удаляется")
    void deleteShifts_removesDayWhenNoShiftsRemain() {
        Shift only = createShift(day, Dep.FIRST, NumberOfShift._28);
        AllTodayShifts dayRow = syncService.ensureExists(day, Dep.FIRST);
        UUID dayId = dayRow.getId();

        deleteService.deleteShifts(List.of(only));

        assertTrue(dataManager.load(AllTodayShifts.class).id(dayId).optional().isEmpty());
    }

    @Test
    @DisplayName("Если смены дня ещё остались, AllTodayShifts сохраняется")
    void deleteShifts_keepsDayWhenShiftsRemain() {
        Shift keep = createShift(day, Dep.FIRST, NumberOfShift._28);
        Shift remove = createShift(day, Dep.FIRST, NumberOfShift._30);
        AllTodayShifts dayRow = syncService.ensureExists(day, Dep.FIRST);

        deleteService.deleteShifts(List.of(remove));

        assertTrue(dataManager.load(AllTodayShifts.class).id(dayRow.getId()).optional().isPresent());
        assertTrue(dataManager.load(Shift.class).id(keep.getId()).optional().isPresent());
    }

    @Test
    @DisplayName("deleteShifts с null/пустым ничего не делает")
    void deleteShifts_ignoresNullAndEmpty() {
        assertDoesNotThrow(() -> deleteService.deleteShifts(null));
        assertDoesNotThrow(() -> deleteService.deleteShifts(List.of()));
        assertDoesNotThrow(() -> deleteService.deleteShifts(Set.of()));
    }

    @Test
    @DisplayName("deleteDay удаляет смены дня вместе с проверками маршрутов")
    void deleteDay_removesRouteChecksWithShifts() {
        Shift checking = createShift(day, Dep.FIRST, NumberOfShift.ANOTHER);
        checking.setTypeOfShift(TypeOfShift.CHECKING);
        checking = dataManager.save(checking);

        RouteCheck check = dataManager.create(RouteCheck.class);
        check.setShift(checking);
        check.setRouteNumber(NumberOfShift._28);
        check.setCheckedAt(LocalTime.of(21, 14));
        check = dataManager.save(check);
        UUID checkId = check.getId();
        UUID shiftId = checking.getId();

        AllTodayShifts dayRow = syncService.ensureExists(day, Dep.FIRST);
        UUID dayId = dayRow.getId();

        deleteService.deleteDay(day, Dep.FIRST);

        assertTrue(dataManager.load(Shift.class).id(shiftId).optional().isEmpty());
        assertTrue(dataManager.load(RouteCheck.class).id(checkId).optional().isEmpty());
        assertTrue(dataManager.load(AllTodayShifts.class).id(dayId).optional().isEmpty());
    }

    private Shift createShift(LocalDate date, Dep department, NumberOfShift number) {
        Shift shift = dataManager.create(Shift.class);
        shift.setDate(date);
        shift.setDepartmentToday(department);
        shift.setNumber(number);
        shift.setTypeOfShift(TypeOfShift.VZVOD_ROUTE);
        shift.setStartTime(LocalTime.of(9, 0));
        shift.setEndTime(LocalTime.of(21, 0));
        return dataManager.save(shift);
    }

    private AdministrativeViolation createAdminViolation(Shift shift) {
        AdministrativeViolation v = dataManager.create(AdministrativeViolation.class);
        v.setShift(shift);
        v.setArticle(ArticleOfAdministrative._18_8);
        v.setImpact(Impact.WITHOUT_IMPACT);
        return dataManager.save(v);
    }

    private CriminalViolation createCriminalViolation(Shift shift) {
        CriminalViolation v = dataManager.create(CriminalViolation.class);
        v.setShift(shift);
        v.setType(TypeOfCriminal.FEDERAL_WANTED);
        v.setImpact(Impact.WITHOUT_IMPACT);
        return dataManager.save(v);
    }

    private void cleanup() {
        List<Shift> shifts = dataManager.load(Shift.class)
                .query("select e from Shift e where e.date = :date")
                .parameter("date", day)
                .list();
        for (Shift shift : shifts) {
            dataManager.load(AdministrativeViolation.class)
                    .query("select v from AdministrativeViolation v where v.shift.id = :id")
                    .parameter("id", shift.getId())
                    .list()
                    .forEach(dataManager::remove);
            dataManager.load(CriminalViolation.class)
                    .query("select v from CriminalViolation v where v.shift.id = :id")
                    .parameter("id", shift.getId())
                    .list()
                    .forEach(dataManager::remove);
            dataManager.load(RouteCheck.class)
                    .query("select c from RouteCheck c where c.shift.id = :id")
                    .parameter("id", shift.getId())
                    .list()
                    .forEach(dataManager::remove);
            dataManager.remove(shift);
        }

        List<AllTodayShifts> days = dataManager.load(AllTodayShifts.class)
                .query("select e from AllTodayShifts e where e.date = :date")
                .parameter("date", day)
                .list();
        days.forEach(dataManager::remove);
    }
}
