package com.company.vzvod.integration;

import com.company.vzvod.entity.DeletedEvent;
import com.company.vzvod.entity.Event;
import com.company.vzvod.entity.EventType;
import com.company.vzvod.service.event_service.EventArchiveService;
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
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test-postgres")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Интеграционный тест EventArchiveService")
class EventArchiveServiceIntegrationTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    @Autowired
    private EventArchiveService eventArchiveService;

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin();
    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.end();
    }

    @Test
    @DisplayName("БЕЗ ВЗВОДА: Event исчезает, DeletedEvent.restorable=true")
    void archive_movesToDeletedRestorable() {
        Event e = newEvent("архив-тест-" + UUID.randomUUID());
        e = dataManager.save(e);

        eventArchiveService.archiveEvents(List.of(e));

        Event found = loadEventById(e.getId());
        assertNull(found, "Event должен быть удалён");

        DeletedEvent archived = singleDeletedByName(e.getName());
        assertTrue(archived.getRestorable());
        assertEquals(e.getName(), archived.getName());
    }

    @Test
    @DisplayName("Восстановление из архива заново создаёт Event и убирает DeletedEvent")
    void restore_returnsToMainList() {
        Event e = newEvent("restore-" + UUID.randomUUID());
        e = dataManager.save(e);
        UUID id = e.getId();
        eventArchiveService.archiveEvents(List.of(e));

        DeletedEvent archived = singleDeletedByName(e.getName());
        eventArchiveService.restoreEvents(List.of(archived));

        DeletedEvent leftover = deletedById(archived.getId());
        assertNull(leftover);
        Event back = singleEventByName(e.getName());
        assertNotNull(back);
        assertNotEquals(id, back.getId());
    }

    @Test
    @DisplayName("Восстановление не трогает записи с restorable=false")
    void restore_skipsNonRestorable() {
        DeletedEvent suppressed = dataManager.create(DeletedEvent.class);
        suppressed.setName("norestore-" + UUID.randomUUID());
        suppressed.setRestorable(false);
        suppressed.setDate(LocalDate.now().plusDays(1));
        suppressed = dataManager.save(suppressed);

        eventArchiveService.restoreEvents(List.of(suppressed));

        DeletedEvent still = deletedById(suppressed.getId());
        assertNotNull(still);
        assertFalse(still.getRestorable());
        assertEquals(0, countEventsNamed(suppressed.getName()));
    }

    @Test
    @DisplayName("УДАЛИТЬ: tombstone с restorable=false, повторное подавление по имени — одна запись")
    void permanentSuppress_idempotent_byName() {
        String name = "tomb-" + UUID.randomUUID();
        Event first = dataManager.save(newEvent(name));

        eventArchiveService.permanentlySuppressEvents(List.of(first));
        DeletedEvent tomb1 = singleDeletedByName(name);
        assertFalse(tomb1.getRestorable());
        assertEquals(1, countAllDeletedNamed(name));

        Event second = dataManager.save(newEvent(name));
        UUID secondId = second.getId();

        eventArchiveService.permanentlySuppressEvents(List.of(second));

        assertNull(loadEventById(secondId));
        assertEquals(1, countAllDeletedNamed(name));
        DeletedEvent tomb2 = singleDeletedByName(name);
        assertEquals(tomb1.getId(), tomb2.getId());
    }

    @Test
    @DisplayName("УДАЛИТЬ в архиве: restorable становится false, запись не в списке restorable=true")
    void permanentSuppressArchived_marksRestorableFalse() {
        Event e = newEvent("arch-final-" + UUID.randomUUID());
        e = dataManager.save(e);
        eventArchiveService.archiveEvents(List.of(e));

        DeletedEvent archived = singleDeletedByName(e.getName());
        assertTrue(archived.getRestorable());

        eventArchiveService.permanentlySuppressArchived(List.of(archived));

        DeletedEvent saved = deletedById(archived.getId());
        assertNotNull(saved);
        assertFalse(saved.getRestorable());

        Collection<DeletedEvent> visibleArchive = loadVisibleArchive(saved.getName());
        assertTrue(visibleArchive.isEmpty());
    }

    private List<DeletedEvent> loadVisibleArchive(String name) {
        return dataManager.load(DeletedEvent.class)
                .query("select e from DeletedEvent e where e.name = :n and e.restorable = true")
                .parameter("n", name)
                .list();
    }

    private Event newEvent(String name) {
        Event e = dataManager.create(Event.class);
        e.setName(name);
        e.setPlace("Площадка");
        e.setDate(LocalDate.now().plusDays(7));
        e.setTime(LocalTime.of(18, 0));
        e.setShiftOfDepartment(1);
        e.setEventType(EventType.OTHER);
        return e;
    }

    private DeletedEvent singleDeletedByName(String name) {
        List<DeletedEvent> list = dataManager.load(DeletedEvent.class)
                .query("select e from DeletedEvent e where e.name = :n")
                .parameter("n", name)
                .list();
        assertEquals(1, list.size());
        return list.get(0);
    }

    private long countAllDeletedNamed(String name) {
        return dataManager.load(DeletedEvent.class)
                .query("select e from DeletedEvent e where e.name = :n")
                .parameter("n", name)
                .list().size();
    }

    private Event singleEventByName(String name) {
        List<Event> list = dataManager.load(Event.class)
                .query("select e from Event e where e.name = :n")
                .parameter("n", name)
                .list();
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    private long countEventsNamed(String name) {
        return dataManager.load(Event.class)
                .query("select e from Event e where e.name = :n")
                .parameter("n", name)
                .list().size();
    }

    private Event loadEventById(UUID id) {
        try {
            return dataManager.load(Event.class).id(id).optional().orElse(null);
        } catch (Exception ignored) {
            return null;
        }
    }

    private DeletedEvent deletedById(UUID id) {
        return dataManager.load(DeletedEvent.class).id(id).optional().orElse(null);
    }
}
