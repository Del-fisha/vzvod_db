package com.company.vzvod.security;

import com.company.vzvod.entity.User;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UiAccessServiceTest {

    @Mock
    private CurrentAuthentication currentAuthentication;

    @Mock
    private RoleGrantedAuthorityUtils roleGrantedAuthorityUtils;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UiAccessService uiAccessService;

    @BeforeEach
    void setUp() {
        when(roleGrantedAuthorityUtils.getDefaultRolePrefix()).thenReturn("resource-");
    }

    @Test
    void hasFullAccessRole_usesAuthenticationAuthoritiesWhenUserTransientIsEmpty() {
        when(currentAuthentication.getAuthentication()).thenReturn(authentication);
        Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("resource-" + FullAccessRole.CODE)
        );
        doReturn(authorities).when(authentication).getAuthorities();

        assertTrue(uiAccessService.hasFullAccessRole());
    }

    @Test
    void hasFullAccessRole_fallsBackToUserAuthoritiesWhenAuthenticationMissing() {
        User user = new User();
        user.setAuthorities(List.of(new SimpleGrantedAuthority(FullAccessRole.CODE)));

        when(currentAuthentication.getAuthentication()).thenReturn(null);
        when(currentAuthentication.getUser()).thenReturn(user);

        assertTrue(uiAccessService.hasFullAccessRole());
    }

    @Test
    void hasFullAccessRole_falseWhenAuthenticationHasNoMatchingRole() {
        when(currentAuthentication.getAuthentication()).thenReturn(authentication);
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("resource-other"));
        doReturn(authorities).when(authentication).getAuthorities();

        assertFalse(uiAccessService.hasFullAccessRole());
    }
}
