package com.company.vzvod.security;

import com.company.vzvod.entity.Post;
import com.company.vzvod.entity.User;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostBasedRoleRefreshFilterTest {

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void nonAdmin_leadershipPost_getsFullAccess_andUiMinimal_only() throws Exception {
        UserPostService userPostService = mock(UserPostService.class);
        when(userPostService.loadPost(any())).thenReturn(Post.COM_VZVOD);

        PostBasedRoleRefreshFilter filter = new PostBasedRoleRefreshFilter(
                new PostBasedRoleResolver(),
                userPostService
        );

        User user = new User();
        user.setId(UUID.randomUUID());

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user,
                "n/a",
                List.of((GrantedAuthority) () -> "unrelated-tech-authority")
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        FilterChain chain = mock(FilterChain.class);
        filter.doFilterInternal(mockRequest(), mockResponse(), chain);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> newAuthCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(chain).doFilter(any(), any());

        var newAuth = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        Set<String> authorities = newAuth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(java.util.stream.Collectors.toSet());

        assertTrue(authorities.contains(UiMinimalRole.CODE));
        assertTrue(authorities.contains(FullAccessRole.CODE));
        assertFalse(authorities.contains(PolicemanRole.CODE));
        assertFalse(authorities.contains(SelfEditUserRole.CODE));
        assertFalse(authorities.contains(PolicemanRowLevelRole.CODE));
        assertTrue(authorities.contains("unrelated-tech-authority"));
    }

    @Test
    void nonAdmin_nonLeadershipPost_getsPolicemanBundle_andUiMinimal() throws Exception {
        UserPostService userPostService = mock(UserPostService.class);
        when(userPostService.loadPost(any())).thenReturn(Post.POLICEMAN);

        PostBasedRoleRefreshFilter filter = new PostBasedRoleRefreshFilter(
                new PostBasedRoleResolver(),
                userPostService
        );

        User user = new User();
        user.setId(UUID.randomUUID());

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user,
                "n/a",
                List.of((GrantedAuthority) () -> FullAccessRole.CODE) // simulate stale authority
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        FilterChain chain = mock(FilterChain.class);
        filter.doFilterInternal(mockRequest(), mockResponse(), chain);

        var newAuth = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        Set<String> authorities = newAuth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(java.util.stream.Collectors.toSet());

        assertTrue(authorities.contains(UiMinimalRole.CODE));
        assertTrue(authorities.contains(PolicemanRole.CODE));
        assertTrue(authorities.contains(SelfEditUserRole.CODE));
        assertTrue(authorities.contains(PolicemanRowLevelRole.CODE));
        assertFalse(authorities.contains(FullAccessRole.CODE));
    }

    @Test
    void admin_always_getsUiMinimal_added_but_other_roles_not_touched() throws Exception {
        UserPostService userPostService = mock(UserPostService.class);
        PostBasedRoleRefreshFilter filter = new PostBasedRoleRefreshFilter(
                new PostBasedRoleResolver(),
                userPostService
        );

        User user = new User();
        user.setId(UUID.randomUUID());

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user,
                "n/a",
                List.of(
                        (GrantedAuthority) () -> ApplicationAdminRole.CODE,
                        (GrantedAuthority) () -> "custom-admin-extra"
                )
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        FilterChain chain = mock(FilterChain.class);
        filter.doFilterInternal(mockRequest(), mockResponse(), chain);

        var newAuth = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        Set<String> authorities = newAuth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(java.util.stream.Collectors.toSet());

        assertTrue(authorities.contains(ApplicationAdminRole.CODE));
        assertTrue(authorities.contains("custom-admin-extra"));
        assertTrue(authorities.contains(UiMinimalRole.CODE));

        verifyNoInteractions(userPostService);
    }

    private static jakarta.servlet.http.HttpServletRequest mockRequest() {
        return mock(jakarta.servlet.http.HttpServletRequest.class);
    }

    private static jakarta.servlet.http.HttpServletResponse mockResponse() {
        return mock(jakarta.servlet.http.HttpServletResponse.class);
    }
}

