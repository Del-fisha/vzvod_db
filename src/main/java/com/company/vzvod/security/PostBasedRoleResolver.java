package com.company.vzvod.security;

import com.company.vzvod.entity.Post;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PostBasedRoleResolver {

    private static final Set<Post> FULL_ACCESS_POSTS = Set.of(
            Post.COM_VZVOD,
            Post.ZAM_COM_VZVOD,
            Post.COM_OTD
    );

    public boolean shouldHaveFullAccess(Post post) {
        return post != null && FULL_ACCESS_POSTS.contains(post);
    }
}

