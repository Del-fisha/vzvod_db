package com.company.vzvod.service;

import com.company.vzvod.entity.User;
import com.company.vzvod.service.dto.UserCacheDto;
import io.jmix.core.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserReadService {

    private static final Logger log = LoggerFactory.getLogger(UserReadService.class);

    private final DataManager dataManager;

    public UserReadService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Cacheable(cacheNames = "userById", key = "#userId")
    public UserCacheDto getUserCached(UUID userId) {
        log.info("CACHE MISS -> loading User {} from DB", userId);

        User user = dataManager.load(User.class)
                .id(userId)
                .one();

        return new UserCacheDto(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getPatronymic(),
                user.getDateOfBirth()
        );
    }
}