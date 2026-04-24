package com.company.vzvod.security;

import com.company.vzvod.entity.Post;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PostBasedRoleResolverTest {

    private final PostBasedRoleResolver resolver = new PostBasedRoleResolver();

    @Test
    void leadershipPosts_getFullAccess() {
        assertTrue(resolver.shouldHaveFullAccess(Post.COM_VZVOD));
        assertTrue(resolver.shouldHaveFullAccess(Post.ZAM_COM_VZVOD));
        assertTrue(resolver.shouldHaveFullAccess(Post.COM_OTD));
    }

    @Test
    void nonLeadershipPosts_doNotGetFullAccess() {
        assertFalse(resolver.shouldHaveFullAccess(Post.POLICEMAN));
        assertFalse(resolver.shouldHaveFullAccess(Post.INTERN));
        assertFalse(resolver.shouldHaveFullAccess(null));
    }
}

