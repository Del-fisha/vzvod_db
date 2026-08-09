package com.company.vzvod.bot;

import com.company.vzvod.bot.dto.BotEventCreateRequest;
import com.company.vzvod.bot.dto.BotEventItem;
import com.company.vzvod.bot.dto.BotEventsResponse;
import com.company.vzvod.entity.Event;
import com.company.vzvod.entity.EventType;
import com.company.vzvod.service.DepartmentConverter;
import com.company.vzvod.service.event_service.EventTypeLoader;
import io.jmix.core.UnconstrainedDataManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class BotMeEventsService {

    private static final int MAX_EVENTS = 200;

    private final UnconstrainedDataManager unconstrainedDataManager;
    private final BotActiveUserChecker activeUserChecker;

    public BotMeEventsService(
            UnconstrainedDataManager unconstrainedDataManager,
            BotActiveUserChecker activeUserChecker
    ) {
        this.unconstrainedDataManager = unconstrainedDataManager;
        this.activeUserChecker = activeUserChecker;
    }

    @Transactional(readOnly = true)
    public BotEventsResponse loadUpcomingEvents(UUID userId) {
        activeUserChecker.requireActive(userId);
        LocalDate today = LocalDate.now();
        List<Event> events = unconstrainedDataManager.load(Event.class)
                .query("select e from Event e where e.date >= :today")
                .parameter("today", today)
                .maxResults(MAX_EVENTS)
                .list();
        events.sort(Comparator
                .comparing(Event::getDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Event::getTime, Comparator.nullsLast(Comparator.naturalOrder())));
        List<BotEventItem> items = new ArrayList<>(events.size());
        for (Event e : events) {
            if (e.getDate() == null) {
                continue;
            }
            items.add(toItem(e));
        }
        return new BotEventsResponse(items);
    }

    @Transactional
    public BotEventItem createEvent(UUID userId, BotEventCreateRequest req) {
        activeUserChecker.requireActive(userId);
        if (req == null || req.name() == null || req.name().isBlank() || req.date() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name and date are required");
        }
        String name = req.name().trim();

        EventType type = EventType.OTHER;
        if (req.eventType() != null && !req.eventType().isBlank()) {
            EventType parsed = EventType.fromId(req.eventType().trim());
            if (parsed == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid eventType");
            }
            type = parsed;
        }
        if (type == EventType.OTHER && EventTypeLoader.isSport(name)) {
            type = EventType.SPORT;
        }

        Event event = unconstrainedDataManager.create(Event.class);
        event.setName(name);
        event.setDate(req.date());
        event.setTime(req.time());
        event.setPlace(req.place());
        event.setDescription(req.description());
        event.setEventType(type);
        event.setShiftOfDepartment(DepartmentConverter.departmentFromDateToInt(req.date()));

        Event saved = unconstrainedDataManager.save(event);
        return toItem(saved);
    }

    private static BotEventItem toItem(Event e) {
        String name = e.getName();
        EventType type = e.getEventType();
        return new BotEventItem(
                e.getId(),
                e.getDate(),
                e.getTime(),
                name == null || name.isBlank() ? "—" : name.trim(),
                e.getPlace(),
                type == null ? null : type.getId(),
                e.getShiftOfDepartment()
        );
    }
}
