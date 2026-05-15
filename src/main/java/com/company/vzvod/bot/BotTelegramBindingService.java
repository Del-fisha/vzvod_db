package com.company.vzvod.bot;

import com.company.vzvod.entity.UserTelegramBinding;
import io.jmix.core.UnconstrainedDataManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class BotTelegramBindingService {

    private final UnconstrainedDataManager unconstrainedDataManager;

    public BotTelegramBindingService(UnconstrainedDataManager unconstrainedDataManager) {
        this.unconstrainedDataManager = unconstrainedDataManager;
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
