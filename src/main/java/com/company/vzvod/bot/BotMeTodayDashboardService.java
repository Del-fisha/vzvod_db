package com.company.vzvod.bot;

import com.company.vzvod.bot.dto.BotTodayDashboardResponse;
import com.company.vzvod.entity.Dep;
import com.company.vzvod.service.DepartmentConverter;
import com.company.vzvod.service.TodayShiftDashboardService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
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

    /**
     * Сводка за конкретную календарную дату (отделение — из {@link DepartmentConverter}).
     * Логика агрегации та же, что у today-dashboard.
     */
    @Transactional(readOnly = true)
    public BotTodayDashboardResponse loadDayDashboard(UUID userId, LocalDate date) {
        activeUserChecker.requireActive(userId);
        if (date == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date is required");
        }
        Dep department = DepartmentConverter.departmentFromDate(date);
        return todayShiftDashboardService.loadBotDashboard(date, department);
    }
}
