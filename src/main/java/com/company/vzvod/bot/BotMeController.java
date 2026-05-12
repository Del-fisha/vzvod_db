package com.company.vzvod.bot;

import com.company.vzvod.bot.dto.BotProfilePatchRequest;
import com.company.vzvod.bot.dto.BotProfileResponse;
import com.company.vzvod.bot.dto.BotShiftsResponse;
import com.company.vzvod.bot.dto.BotVacationsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/bot/me")
public class BotMeController {

    private final BotApiKeyAuthorizer apiKeyAuthorizer;
    private final BotMeProfileService botMeProfileService;
    private final BotMeShiftsVocationsService botMeShiftsVocationsService;

    public BotMeController(
            BotApiKeyAuthorizer apiKeyAuthorizer,
            BotMeProfileService botMeProfileService,
            BotMeShiftsVocationsService botMeShiftsVocationsService
    ) {
        this.apiKeyAuthorizer = apiKeyAuthorizer;
        this.botMeProfileService = botMeProfileService;
        this.botMeShiftsVocationsService = botMeShiftsVocationsService;
    }

    @GetMapping("/profile")
    public ResponseEntity<BotProfileResponse> profile(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader
    ) {
        apiKeyAuthorizer.verify(apiKey);
        long chatId = parseTelegramChatId(telegramChatIdHeader);
        return ResponseEntity.ok(botMeProfileService.loadProfile(chatId));
    }

    @GetMapping("/shifts")
    public ResponseEntity<BotShiftsResponse> shifts(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader
    ) {
        apiKeyAuthorizer.verify(apiKey);
        long chatId = parseTelegramChatId(telegramChatIdHeader);
        return ResponseEntity.ok(botMeShiftsVocationsService.loadShifts(chatId));
    }

    @GetMapping("/vacations")
    public ResponseEntity<BotVacationsResponse> vacations(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader
    ) {
        apiKeyAuthorizer.verify(apiKey);
        long chatId = parseTelegramChatId(telegramChatIdHeader);
        return ResponseEntity.ok(botMeShiftsVocationsService.loadVacations(chatId));
    }

    @PutMapping("/profile")
    public ResponseEntity<BotProfileResponse> updateProfile(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader,
            @RequestBody(required = false) BotProfilePatchRequest body
    ) {
        apiKeyAuthorizer.verify(apiKey);
        long chatId = parseTelegramChatId(telegramChatIdHeader);
        return ResponseEntity.ok(botMeProfileService.updateProfile(chatId, body));
    }

    private static long parseTelegramChatId(String header) {
        if (header == null || header.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "X-Telegram-Chat-Id is required");
        }
        try {
            return Long.parseLong(header.trim());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid X-Telegram-Chat-Id");
        }
    }
}
