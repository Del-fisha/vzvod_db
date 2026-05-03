package com.company.vzvod.service.event_service;

import com.company.vzvod.entity.DeletedEvent;
import com.company.vzvod.entity.Event;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
public class EventArchiveService {

    private final Metadata metadata;
    private final DataManager dataManager;

    public EventArchiveService(Metadata metadata, DataManager dataManager) {
        this.metadata = metadata;
        this.dataManager = dataManager;
    }

    /**
     * Перенос в «Мероприятия без взвода» (можно вернуть).
     */
    public void archiveEvents(Collection<Event> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        for (Event e : events) {
            DeletedEvent de = metadata.create(DeletedEvent.class);
            copyEventToDeletedEvent(e, de);
            de.setRestorable(true);

            saveAndRemove(de, e);
        }
    }

    /**
     * Удаление из основного списка с блокировкой повторного появления по имени (Kafka и т.д.).
     * Идемпотентно при повторных вызовах для того же имени.
     */
    public void permanentlySuppressEvents(Collection<Event> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        for (Event e : events) {
            upsertNonRestorableTombstoneFromEvent(e);
            if (e.getId() != null) {
                dataManager.remove(e);
            }
        }
    }

    /**
     * Из «Без взвода»: окончательно скрыть и не давать импорту вернуть событие.
     */
    public void permanentlySuppressArchived(Collection<DeletedEvent> deletedEvents) {
        if (deletedEvents == null || deletedEvents.isEmpty()) {
            return;
        }
        for (DeletedEvent de : deletedEvents) {
            de.setRestorable(false);
            dataManager.save(de);
        }
    }

    public void restoreEvents(Collection<DeletedEvent> deletedEvents) {
        if (deletedEvents == null || deletedEvents.isEmpty()) {
            return;
        }
        for (DeletedEvent de : deletedEvents) {
            if (!Boolean.TRUE.equals(de.getRestorable())) {
                continue;
            }
            Event restored = metadata.create(Event.class);
            copyDeletedEventToEvent(de, restored);

            saveAndRemove(restored, de);
        }
    }

    private void upsertNonRestorableTombstoneFromEvent(Event sourceEvent) {
        String name = sourceEvent.getName();
        if (name == null) {
            return;
        }
        Optional<DeletedEvent> existing = dataManager.load(DeletedEvent.class)
                .query("select e from DeletedEvent e where e.name = :name")
                .parameter("name", name)
                .maxResults(1)
                .optional();

        DeletedEvent de;
        if (existing.isPresent()) {
            de = existing.get();
            copyEventToDeletedEvent(sourceEvent, de);
        } else {
            de = metadata.create(DeletedEvent.class);
            copyEventToDeletedEvent(sourceEvent, de);
        }
        de.setRestorable(false);
        dataManager.save(de);
    }

    private void copyEventToDeletedEvent(Event src, DeletedEvent dst) {
        dst.setOriginalEventId(src.getId());
        dst.setEventType(src.getEventType());
        dst.setPlace(src.getPlace());
        dst.setName(src.getName());
        dst.setDate(src.getDate());
        dst.setTime(src.getTime());
        dst.setShiftOfDepartment(src.getShiftOfDepartment());
        dst.setDescription(src.getDescription());
    }

    private void copyDeletedEventToEvent(DeletedEvent src, Event dst) {
        dst.setEventType(src.getEventType());
        dst.setPlace(src.getPlace());
        dst.setName(src.getName());
        dst.setDate(src.getDate());
        dst.setTime(src.getTime());
        dst.setShiftOfDepartment(src.getShiftOfDepartment());
        dst.setDescription(src.getDescription());
    }

    private void saveAndRemove(Object toSave, Object toRemove) {
        dataManager.save(toSave);
        dataManager.remove(toRemove);
    }
}
