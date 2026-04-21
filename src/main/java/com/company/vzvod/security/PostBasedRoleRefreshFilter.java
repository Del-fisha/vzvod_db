package com.company.vzvod.security;

import com.company.vzvod.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class PostBasedRoleRefreshFilter extends OncePerRequestFilter {

    private final PostBasedRoleResolver roleResolver;
    private final UserPostService userPostService;

    public PostBasedRoleRefreshFilter(PostBasedRoleResolver roleResolver, UserPostService userPostService) {
        this.roleResolver = roleResolver;
        this.userPostService = userPostService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof User user) {
            if (isAdmin(authentication.getAuthorities())) {
                ensureUiMinimal(authentication, user);
            } else {
                refreshAuthoritiesIfNeeded(authentication, user);
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAdmin(Collection<? extends GrantedAuthority> authorities) {
        return hasAuthority(authorities, ApplicationAdminRole.CODE);
    }

    private void ensureUiMinimal(Authentication authentication, User user) {
        if (hasAuthority(authentication.getAuthorities(), UiMinimalRole.CODE)) {
            return;
        }

        List<GrantedAuthority> newAuthorities = new ArrayList<>(authentication.getAuthorities().size() + 1);
        newAuthorities.addAll(authentication.getAuthorities());
        newAuthorities.add(() -> UiMinimalRole.CODE);

        user.setAuthorities(newAuthorities);

        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                authentication.getPrincipal(),
                authentication.getCredentials(),
                newAuthorities
        );
        ((UsernamePasswordAuthenticationToken) newAuth).setDetails(authentication.getDetails());
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    private void refreshAuthoritiesIfNeeded(Authentication authentication, User user) {
        UUID userId = user.getId();
        if (userId == null) {
            return;
        }

        var post = userPostService.loadPost(userId);
        boolean shouldBeFull = roleResolver.shouldHaveFullAccess(post);

        Set<String> desired = new HashSet<>();
        desired.add(UiMinimalRole.CODE);
        if (shouldBeFull) {
            desired.add(FullAccessRole.CODE);
        } else {
            desired.add(PolicemanRole.CODE);
            desired.add(SelfEditUserRole.CODE);
            desired.add(PolicemanRowLevelRole.CODE);
        }

        List<GrantedAuthority> newAuthorities = rebuildAuthorities(authentication.getAuthorities(), desired);
        if (!sameAuthorities(authentication.getAuthorities(), newAuthorities)) {
            user.setAuthorities(newAuthorities);
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    authentication.getPrincipal(),
                    authentication.getCredentials(),
                    newAuthorities
            );
            ((UsernamePasswordAuthenticationToken) newAuth).setDetails(authentication.getDetails());
            SecurityContextHolder.getContext().setAuthentication(newAuth);
        }
    }

    private static boolean hasAuthority(Collection<? extends GrantedAuthority> authorities, String authority) {
        for (GrantedAuthority ga : authorities) {
            if (authority.equals(ga.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private static List<GrantedAuthority> rebuildAuthorities(
            Collection<? extends GrantedAuthority> existing,
            Set<String> desiredRoleCodes
    ) {
        // Keep unrelated authorities (e.g. technical ones), but normalize our managed role set.
        Set<String> managed = Set.of(
                FullAccessRole.CODE,
                PolicemanRole.CODE,
                SelfEditUserRole.CODE,
                PolicemanRowLevelRole.CODE,
                UiMinimalRole.CODE
        );

        List<GrantedAuthority> result = new ArrayList<>();
        for (GrantedAuthority ga : existing) {
            if (!managed.contains(ga.getAuthority())) {
                result.add(ga);
            }
        }

        // Add desired roles (as simple GrantedAuthority implementations).
        for (String role : desiredRoleCodes) {
            result.add(() -> role);
        }

        return result;
    }

    private static boolean sameAuthorities(
            Collection<? extends GrantedAuthority> a,
            Collection<? extends GrantedAuthority> b
    ) {
        Set<String> sa = toSet(a);
        Set<String> sb = toSet(b);
        return sa.equals(sb);
    }

    private static Set<String> toSet(Collection<? extends GrantedAuthority> authorities) {
        Set<String> s = new HashSet<>();
        for (GrantedAuthority ga : authorities) {
            s.add(ga.getAuthority());
        }
        return s;
    }
}

