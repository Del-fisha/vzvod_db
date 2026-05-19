package com.company.vzvod.security;

import com.company.vzvod.entity.Post;
import com.company.vzvod.entity.ServiceInfo;
import io.jmix.core.UnconstrainedDataManager;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserPostService {

    private final UnconstrainedDataManager dataManager;

    public UserPostService(UnconstrainedDataManager dataManager) {
        this.dataManager = dataManager;
    }

    public Post loadPost(UUID userId) {
        ServiceInfo serviceInfo = dataManager.load(ServiceInfo.class)
                .query("select si from ServiceInfo si where si.user.id = :userId")
                .parameter("userId", userId)
                .optional()
                .orElse(null);
        return serviceInfo == null ? null : serviceInfo.getPost();
    }
}

