package com.company.vzvod.bot;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class BotTelegramAccessClosedHandler {

    private final BotTelegramMicroserviceClient botClient;

    public BotTelegramAccessClosedHandler(BotTelegramMicroserviceClient botClient) {
        this.botClient = botClient;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAccessClosed(BotTelegramAccessClosedEvent event) {
        botClient.revokeAccess(event.chatId(), event.message());
    }
}
