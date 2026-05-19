package com.company.vzvod.security;

import com.company.vzvod.entity.Post;
import com.company.vzvod.entity.User;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Syncs post-based resource roles in DB. Invoked at login only — not from a servlet filter:
 * Vaadin sends concurrent UIDL requests; rewriting {@code SecurityContext} on each request
 * caused random logouts every few minutes.
 */
@Component
public class PostBasedRoleAuthorityService {

    private final PostBasedRoleResolver roleResolver;
    private final UserPostService userPostService;
    private final PostBasedRoleAssignmentService roleAssignmentService;

    public PostBasedRoleAuthorityService(
            PostBasedRoleResolver roleResolver,
            UserPostService userPostService,
            PostBasedRoleAssignmentService roleAssignmentService
    ) {
        this.roleResolver = roleResolver;
        this.userPostService = userPostService;
        this.roleAssignmentService = roleAssignmentService;
    }

    public void syncRoleAssignmentsForUser(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().isBlank()) {
            return;
        }
        UUID userId = user.getId();
        if (userId == null) {
            return;
        }
        Post post = userPostService.loadPost(userId);
        roleAssignmentService.ensurePostBasedRole(
                user.getUsername(),
                roleResolver.shouldHaveFullAccess(post)
        );
    }
}
