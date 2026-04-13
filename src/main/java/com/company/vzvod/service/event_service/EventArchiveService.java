package com.company.vzvod.service.event_service;

import com.company.vzvod.entity.DeletedEvent;
import com.company.vzvod.entity.Event;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class EventArchiveService {

    private final Metadata metadata;
    private final DataManager dataManager;

    public EventArchiveService(Metadata metadata, DataManager dataManager) {
        this.metadata = metadata;
        this.dataManager = dataManager;
    }

    public void archiveEvents(Collection<Event> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        for (Event e : events) {
            DeletedEvent de = metadata.create(DeletedEvent.class);
            copyEventToDeletedEvent(e, de);

            saveAndRemove(de, e);
        }
    }

    public void restoreEvents(Collection<DeletedEvent> deletedEvents) {
        if (deletedEvents == null || deletedEvents.isEmpty()) {
            return;
        }
        for (DeletedEvent de : deletedEvents) {
            Event restored = metadata.create(Event.class);
            copyDeletedEventToEvent(de, restored);

            saveAndRemove(restored, de);
        }
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