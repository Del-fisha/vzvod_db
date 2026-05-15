package com.company.vzvod.bot;

import com.company.vzvod.entity.Contacts;
import com.company.vzvod.entity.StatusInService;
import com.company.vzvod.entity.UserTelegramBinding;
import io.jmix.core.UnconstrainedDataManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BotTelegramAccessService {

    private final BotTelegramBindingService bindingService;
    private final BotActiveUserChecker activeUserChecker;
    private final UnconstrainedDataManager unconstrainedDataManager;
    private final ApplicationEventPublisher eventPublisher;

    public BotTelegramAccessService(
            BotTelegramBindingService bindingService,
            BotActiveUserChecker activeUserChecker,
            UnconstrainedDataManager unconstrainedDataManager,
            ApplicationEventPublisher eventPublisher
    ) {
        this.bindingService = bindingService;
        this.activeUserChecker = activeUserChecker;
        this.unconstrainedDataManager = unconstrainedDataManager;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void closeAccess(UUID userId, String message) {
        bindingService.revokeByUserId(userId)
                .ifPresent(chatId -> eventPublisher.publishEvent(new BotTelegramAccessClosedEvent(chatId, message)));
    }

    public boolean isEligibleForBot(UUID userId) {
        if (!isActive(userId)) {
            return false;
        }
        Contacts contacts = unconstrainedDataManager.load(Contacts.class)
                .query("select c from Contacts c where c.user.id = :uid")
                .parameter("uid", userId)
                .optional()
                .orElse(null);
        if (contacts == null) {
            return false;
        }
        String phone = contacts.getPhoneNumber();
        return phone != null && !phone.isBlank();
    }

    @Transactional
    public int reconcileStaleBindings() {
        List<UserTelegramBinding> bindings = unconstrainedDataManager.load(UserTelegramBinding.class)
                .query("select b from UserTelegramBinding b join fetch b.user")
                .list();
        int closed = 0;
        for (UserTelegramBinding binding : bindings) {
            UUID userId = binding.getUser().getId();
            if (isEligibleForBot(userId)) {
                continue;
            }
            String message = resolveReconciliationMessage(userId);
            bindingService.revokeByUserId(userId)
                    .ifPresent(chatId -> eventPublisher.publishEvent(new BotTelegramAccessClosedEvent(chatId, message)));
            closed++;
        }
        return closed;
    }

    private String resolveReconciliationMessage(UUID userId) {
        if (!isActive(userId)) {
            return BotTelegramAccessMessages.NOT_ACTIVE;
        }
        return BotTelegramAccessMessages.RECONCILIATION;
    }

    private boolean isActive(UUID userId) {
        try {
            activeUserChecker.requireActive(userId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
