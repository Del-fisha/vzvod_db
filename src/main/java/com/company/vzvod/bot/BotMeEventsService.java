package com.company.vzvod.bot;

import com.company.vzvod.bot.dto.BotEventItem;
import com.company.vzvod.bot.dto.BotEventsResponse;
import com.company.vzvod.entity.Event;
import com.company.vzvod.entity.UserTelegramBinding;
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
    public BotEventsResponse loadUpcomingEvents(long telegramChatId) {
        requireActiveBinding(telegramChatId);
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
            String name = e.getName();
            items.add(new BotEventItem(e.getDate(), name == null || name.isBlank() ? "—" : name.trim()));
        }
        return new BotEventsResponse(items);
    }

    private void requireActiveBinding(long telegramChatId) {
        UserTelegramBinding binding = unconstrainedDataManager.load(UserTelegramBinding.class)
                .query("select b from UserTelegramBinding b where b.chatId = :cid")
                .parameter("cid", telegramChatId)
                .optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "telegram chat not bound"));
        UUID userId = binding.getUser().getId();
        activeUserChecker.requireActive(userId);
    }
}
