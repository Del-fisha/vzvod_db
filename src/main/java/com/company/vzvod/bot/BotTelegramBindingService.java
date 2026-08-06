package com.company.vzvod.bot;

import com.company.vzvod.entity.UserTelegramBinding;
import io.jmix.core.UnconstrainedDataManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
public class BotTelegramBindingService {

    private final UnconstrainedDataManager unconstrainedDataManager;
    private final BotActiveUserChecker activeUserChecker;

    public BotTelegramBindingService(
            UnconstrainedDataManager unconstrainedDataManager,
            BotActiveUserChecker activeUserChecker
    ) {
        this.unconstrainedDataManager = unconstrainedDataManager;
        this.activeUserChecker = activeUserChecker;
    }

    public UUID requireActiveUserIdByChatId(long chatId) {
        UserTelegramBinding binding = unconstrainedDataManager.load(UserTelegramBinding.class)
                .query("select b from UserTelegramBinding b where b.chatId = :cid")
                .parameter("cid", chatId)
                .optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "telegram chat not bound"));
        UUID userId = binding.getUser().getId();
        activeUserChecker.requireActive(userId);
        return userId;
    }

    public Optional<Long> findChatIdByUserId(UUID userId) {
        return unconstrainedDataManager.load(UserTelegramBinding.class)
                .query("select b from UserTelegramBinding b where b.user.id = :uid")
                .parameter("uid", userId)
                .optional()
                .map(UserTelegramBinding::getChatId);
    }

    @Transactional
    public Optional<Long> revokeByUserId(UUID userId) {
        Optional<UserTelegramBinding> binding = unconstrainedDataManager.load(UserTelegramBinding.class)
                .query("select b from UserTelegramBinding b where b.user.id = :uid")
                .parameter("uid", userId)
                .optional();
        if (binding.isEmpty()) {
            return Optional.empty();
        }
        long chatId = binding.get().getChatId();
        unconstrainedDataManager.remove(binding.get());
        return Optional.of(chatId);
    }

    @Transactional
    public Optional<Long> revokeByChatId(long chatId) {
        Optional<UserTelegramBinding> binding = unconstrainedDataManager.load(UserTelegramBinding.class)
                .query("select b from UserTelegramBinding b where b.chatId = :cid")
                .parameter("cid", chatId)
                .optional();
        if (binding.isEmpty()) {
            return Optional.empty();
        }
        long revokedChatId = binding.get().getChatId();
        unconstrainedDataManager.remove(binding.get());
        return Optional.of(revokedChatId);
    }
}
