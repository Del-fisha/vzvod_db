package com.company.vzvod.bot;

import com.company.vzvod.bot.dto.BotTodayDashboardResponse;
import com.company.vzvod.service.TodayShiftDashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Дашборд операционных суток для мобильного API: доступен любому аутентифицированному
 * пользователю приложения (не только с полным доступом).
 */
@Service
public class BotMeTodayDashboardService {

    private final BotActiveUserChecker activeUserChecker;
    private final TodayShiftDashboardService todayShiftDashboardService;

    public BotMeTodayDashboardService(
            BotActiveUserChecker activeUserChecker,
            TodayShiftDashboardService todayShiftDashboardService
    ) {
        this.activeUserChecker = activeUserChecker;
        this.todayShiftDashboardService = todayShiftDashboardService;
    }

    @Transactional(readOnly = true)
    public BotTodayDashboardResponse loadTodayDashboard(UUID userId) {
        activeUserChecker.requireActive(userId);
        return todayShiftDashboardService.loadBotDashboard();
    }
}
