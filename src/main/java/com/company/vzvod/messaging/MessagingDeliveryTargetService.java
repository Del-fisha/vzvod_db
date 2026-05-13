package com.company.vzvod.messaging;

import com.company.vzvod.entity.UserTelegramBinding;
import com.company.vzvod.messaging.dto.MessagingDeliveryTargetDto;
import io.jmix.core.UnconstrainedDataManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class MessagingDeliveryTargetService {

    private final UnconstrainedDataManager dataManager;

    public MessagingDeliveryTargetService(UnconstrainedDataManager dataManager) {
        this.dataManager = dataManager;
    }

    public List<MessagingDeliveryTargetDto> resolveTelegramTargets(Set<UUID> recipientUserIds) {
        if (recipientUserIds == null || recipientUserIds.isEmpty()) {
            return List.of();
        }

        List<UUID> userIds = new ArrayList<>(new LinkedHashSet<>(recipientUserIds));
        List<UserTelegramBinding> bindings = dataManager.load(UserTelegramBinding.class)
                .query("select b from UserTelegramBinding b where b.user.id in :userIds")
                .parameter("userIds", userIds)
                .list();

        List<MessagingDeliveryTargetDto> targets = new ArrayList<>(bindings.size());
        for (UserTelegramBinding binding : bindings) {
            if (binding.getUser() == null || binding.getChatId() == null) {
                continue;
            }
            targets.add(new MessagingDeliveryTargetDto(binding.getUser().getId(), binding.getChatId()));
        }
        return targets;
    }
}
