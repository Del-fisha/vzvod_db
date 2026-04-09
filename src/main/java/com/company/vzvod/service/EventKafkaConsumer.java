package com.company.vzvod.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class EventKafkaConsumer {

//    private static final Logger log = LoggerFactory.getLogger(EventKafkaConsumer.class);
//
//    @KafkaListener(topics = "events", groupId = "event-parser-group")
//    public void consumeEvent(EventDto eventDto) {
//        log.info("Получено событие: {}", eventDto);
//    }
}