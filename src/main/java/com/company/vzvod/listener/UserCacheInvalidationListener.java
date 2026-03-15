package com.company.vzvod.listener;

import com.company.vzvod.entity.User;
import io.jmix.core.event.EntityChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Component
public class UserCacheInvalidationListener {

    private static final Logger log = LoggerFactory.getLogger(UserCacheInvalidationListener.class);

    private final CacheManager cacheManager;

    public UserCacheInvalidationListener(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onUserChangedAfterCommit(EntityChangedEvent<User> event) {
        try {
            Cache cache = cacheManager.getCache("userById");
            if (cache == null) {
                return;
            }

            UUID userId = (UUID) event.getEntityId().getValue();
            cache.evict(userId);
        } catch (Exception e) {
            log.error("Error invalidating userById cache", e);
        }
    }
}