package com.company.vzvod.mobile;

import com.company.vzvod.bot.BotActiveUserChecker;
import com.company.vzvod.entity.Post;
import com.company.vzvod.entity.User;
import com.company.vzvod.entity.UserMobileBinding;
import com.company.vzvod.mobile.dto.MobileAuthRequest;
import com.company.vzvod.mobile.dto.MobileAuthResponse;
import com.company.vzvod.security.PostBasedRoleResolver;
import com.company.vzvod.security.UserPostService;
import io.jmix.core.UnconstrainedDataManager;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class MobileAuthService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UnconstrainedDataManager dataManager;
    private final PasswordEncoder passwordEncoder;
    private final BotActiveUserChecker activeUserChecker;
    private final UserPostService userPostService;
    private final PostBasedRoleResolver roleResolver;

    public MobileAuthService(
            UnconstrainedDataManager dataManager,
            PasswordEncoder passwordEncoder,
            BotActiveUserChecker activeUserChecker,
            UserPostService userPostService,
            PostBasedRoleResolver roleResolver
    ) {
        this.dataManager = dataManager;
        this.passwordEncoder = passwordEncoder;
        this.activeUserChecker = activeUserChecker;
        this.userPostService = userPostService;
        this.roleResolver = roleResolver;
    }

    @Transactional
    public MobileAuthResponse authenticate(MobileAuthRequest request) {
        if (request == null || request.username() == null || request.username().isBlank()
                || request.password() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username and password required");
        }
        String username = request.username().trim();

        User user = dataManager.load(User.class)
                .query("select u from User u where u.username = :uname")
                .parameter("uname", username)
                .optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials"));

        String hash = user.getPassword();
        if (hash == null || hash.isBlank() || !passwordEncoder.matches(request.password(), hash)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials");
        }

        activeUserChecker.requireActive(user.getId());

        Post post = userPostService.loadPost(user.getId());
        boolean fullAccess = roleResolver.shouldHaveFullAccess(post);
        String role = fullAccess ? "FULL_ACCESS" : "POLICEMAN";

        String token = issueToken(user, request.deviceId());

        // reload display name fields
        User named = dataManager.load(User.class)
                .id(user.getId())
                .fetchPlan(fp -> fp.add("firstName").add("lastName").add("patronymic").add("username"))
                .one();

        return new MobileAuthResponse(
                named.getId(),
                named.getDisplayName(),
                named.getUsername(),
                role,
                fullAccess,
                token
        );
    }

    @Transactional(readOnly = true)
    public UUID requireActiveUserIdByToken(String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "X-Mobile-Token required");
        }
        UserMobileBinding binding = dataManager.load(UserMobileBinding.class)
                .query("select b from UserMobileBinding b where b.token = :t")
                .parameter("t", token.trim())
                .optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid token"));
        UUID userId = binding.getUser().getId();
        activeUserChecker.requireActive(userId);
        return userId;
    }

    @Transactional
    public void touchLastSeen(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        dataManager.load(UserMobileBinding.class)
                .query("select b from UserMobileBinding b where b.token = :t")
                .parameter("t", token.trim())
                .optional()
                .ifPresent(b -> {
                    b.setLastSeenAt(OffsetDateTime.now(ZoneOffset.UTC));
                    dataManager.save(b);
                });
    }

    private String issueToken(User user, String deviceId) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        String token = randomToken();

        UserMobileBinding existing = dataManager.load(UserMobileBinding.class)
                .query("select b from UserMobileBinding b where b.user.id = :uid")
                .parameter("uid", user.getId())
                .optional()
                .orElse(null);

        if (existing == null) {
            UserMobileBinding b = dataManager.create(UserMobileBinding.class);
            b.setUser(user);
            b.setToken(token);
            b.setDeviceId(blankToNull(deviceId));
            b.setRegisteredAt(now);
            b.setLastSeenAt(now);
            dataManager.save(b);
        } else {
            existing.setToken(token);
            existing.setDeviceId(blankToNull(deviceId));
            existing.setRegisteredAt(now);
            existing.setLastSeenAt(now);
            dataManager.save(existing);
        }
        return token;
    }

    private static String randomToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
