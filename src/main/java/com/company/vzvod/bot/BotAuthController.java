package com.company.vzvod.bot;

import com.company.vzvod.bot.dto.BotAuthRequest;
import com.company.vzvod.bot.dto.BotAuthResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/bot")
public class BotAuthController {

    private final BotApiKeyAuthorizer apiKeyAuthorizer;
    private final BotAuthService botAuthService;

    public BotAuthController(BotApiKeyAuthorizer apiKeyAuthorizer, BotAuthService botAuthService) {
        this.apiKeyAuthorizer = apiKeyAuthorizer;
        this.botAuthService = botAuthService;
    }

    @PostMapping("/auth")
    public ResponseEntity<BotAuthResponse> auth(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestBody BotAuthRequest request
    ) {
        apiKeyAuthorizer.verify(apiKey);
        try {
            return ResponseEntity.ok(botAuthService.authenticate(request.phoneNumber(), request.chatId()));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }
}
