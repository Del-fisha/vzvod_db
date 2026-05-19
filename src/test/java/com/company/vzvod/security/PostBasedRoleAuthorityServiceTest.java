package com.company.vzvod.security;

import com.company.vzvod.entity.Post;
import com.company.vzvod.entity.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.*;

class PostBasedRoleAuthorityServiceTest {

    @Test
    void leadershipPost_assignsFullAccessInDb() {
        UserPostService userPostService = mock(UserPostService.class);
        when(userPostService.loadPost(any())).thenReturn(Post.COM_VZVOD);

        PostBasedRoleAssignmentService roleAssignmentService = mock(PostBasedRoleAssignmentService.class);

        PostBasedRoleAuthorityService service = new PostBasedRoleAuthorityService(
                new PostBasedRoleResolver(),
                userPostService,
                roleAssignmentService
        );

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("commander");

        service.syncRoleAssignmentsForUser(user);

        verify(roleAssignmentService).ensurePostBasedRole("commander", true);
    }

    @Test
    void policemanPost_doesNotAssignFullAccessInDb() {
        UserPostService userPostService = mock(UserPostService.class);
        when(userPostService.loadPost(any())).thenReturn(Post.POLICEMAN);

        PostBasedRoleAssignmentService roleAssignmentService = mock(PostBasedRoleAssignmentService.class);

        PostBasedRoleAuthorityService service = new PostBasedRoleAuthorityService(
                new PostBasedRoleResolver(),
                userPostService,
                roleAssignmentService
        );

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("officer");

        service.syncRoleAssignmentsForUser(user);

        verify(roleAssignmentService).ensurePostBasedRole("officer", false);
    }
}
