package com.company.vzvod.service;

import com.company.vzvod.entity.Event;
import com.company.vzvod.entity.EventType;
import com.company.vzvod.service.event_service.EventTypeLoader;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import practice.dto.EventDto;

@Component
public class EventKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventKafkaConsumer.class);
    private final DataManager dataManager;

    public EventKafkaConsumer(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    @KafkaListener(topics = "events", groupId = "event-parser-group")
    public void consumeEvent(EventDto dto) {
        log.info("Получено событие: {}", dto);

        systemAuthenticator.runWithSystem(() -> {
            Event event = dataManager.load(Event.class)
                    .query("select e from Event e where e.name = :name")
                    .parameter("name", dto.getName())
                    .optional()
                    .orElseGet(() -> dataManager.create(Event.class));

            event.setName(dto.getName());
            if (event.getEventType() == null) {
                event.setEventType(EventType.OTHER);
            }
            if (event.getEventType() == EventType.OTHER) {
                boolean isSport = EventTypeLoader.isSport(event.getName());
                if (isSport) {
                    event.setEventType(EventType.SPORT);
                }
            }

            event.setDate(dto.getDate());
            event.setTime(dto.getTime());
            event.setShiftOfDepartment(DepartmentConverter.departmentFromDateToInt(dto.getDate()));
            event.setPlace(dto.getPlace());



            dataManager.save(event);
        });
    }
}