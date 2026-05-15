package com.company.vzvod.job;

import com.company.vzvod.bot.BotTelegramAccessService;
import io.jmix.core.security.Authenticated;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class BotTelegramBindingDailyReconciliationJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(BotTelegramBindingDailyReconciliationJob.class);

    @Autowired
    private BotTelegramAccessService botTelegramAccessService;

    @Authenticated
    @Override
    public void execute(JobExecutionContext context) {
        int closed = botTelegramAccessService.reconcileStaleBindings();
        if (closed > 0) {
            log.info("Ежедневная сверка Telegram-привязок: закрыто доступов — {}", closed);
        }
    }
}
