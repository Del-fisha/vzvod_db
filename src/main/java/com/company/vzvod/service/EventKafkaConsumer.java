package com.company.vzvod.service;

import com.company.vzvod.entity.Event;
import com.company.vzvod.entity.EventType;
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
            Event event = dataManager.create(Event.class);
            event.setName(dto.getName());
            event.setPlace(dto.getPlace());
            event.setDate(dto.getDate());
            event.setTime(dto.getTime());


            if (dto.getName() != null && dto.getName().toLowerCase().contains("концерт")) {
                event.setEventType(EventType.CONCERT);
            } else if (dto.getPlace() != null && dto.getPlace().toLowerCase().contains("арена")) {
                event.setEventType(EventType.SPORT);
            } else {
                event.setEventType(EventType.OTHER);
            }

            dataManager.save(event);
        });
    }
}