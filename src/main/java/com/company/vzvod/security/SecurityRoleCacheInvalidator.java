package com.company.vzvod.security;

import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RowLevelRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Jmix caches role definitions in Spring Cache (e.g. Redis). On redeploy, the cache may contain
 * stale role policies, so UI permissions won't reflect code changes until cache eviction.
 * Clearing caches on startup guarantees that design-time role changes are applied immediately.
 */
@Component
public class SecurityRoleCacheInvalidator {

    private static final Logger log = LoggerFactory.getLogger(SecurityRoleCacheInvalidator.class);

    private final ResourceRoleRepository resourceRoleRepository;
    private final RowLevelRoleRepository rowLevelRoleRepository;

    public SecurityRoleCacheInvalidator(ResourceRoleRepository resourceRoleRepository,
                                        RowLevelRoleRepository rowLevelRoleRepository) {
        this.resourceRoleRepository = resourceRoleRepository;
        this.rowLevelRoleRepository = rowLevelRoleRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void invalidate() {
        try {
            resourceRoleRepository.invalidateCache();
            rowLevelRoleRepository.invalidateCache();
            log.info("Security role caches invalidated (resource + row-level)");
        } catch (Exception e) {
            log.warn("Failed to invalidate security role caches: {}", e.toString());
        }
    }
}

