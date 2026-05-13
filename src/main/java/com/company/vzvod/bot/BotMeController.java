package com.company.vzvod.bot;

import com.company.vzvod.bot.dto.BotProfilePatchRequest;
import com.company.vzvod.bot.dto.BotProfileResponse;
import com.company.vzvod.bot.dto.BotColleaguesResponse;
import com.company.vzvod.bot.dto.BotShiftEndTimeRequest;
import com.company.vzvod.bot.dto.BotShiftItem;
import com.company.vzvod.bot.dto.BotShiftUpsertRequest;
import com.company.vzvod.bot.dto.BotShiftsResponse;
import com.company.vzvod.bot.dto.BotVacationsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

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

    @GetMapping("/colleagues")
    public ResponseEntity<BotColleaguesResponse> colleagues(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader,
            @RequestParam("department") int department,
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {
        apiKeyAuthorizer.verify(apiKey);
        long chatId = parseTelegramChatId(telegramChatIdHeader);
        return ResponseEntity.ok(botMeShiftsVocationsService.loadColleagues(chatId, department, page));
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

    @GetMapping("/shifts/{shiftId}")
    public ResponseEntity<BotShiftItem> shift(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader,
            @PathVariable("shiftId") UUID shiftId
    ) {
        apiKeyAuthorizer.verify(apiKey);
        long chatId = parseTelegramChatId(telegramChatIdHeader);
        return ResponseEntity.ok(botMeShiftsVocationsService.loadShift(chatId, shiftId));
    }

    @PostMapping("/shifts")
    public ResponseEntity<BotShiftItem> createShift(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader,
            @RequestBody(required = false) BotShiftUpsertRequest body
    ) {
        apiKeyAuthorizer.verify(apiKey);
        long chatId = parseTelegramChatId(telegramChatIdHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(botMeShiftsVocationsService.createShift(chatId, body));
    }

    @PutMapping("/shifts/{shiftId}")
    public ResponseEntity<BotShiftItem> updateShift(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader,
            @PathVariable("shiftId") UUID shiftId,
            @RequestBody(required = false) BotShiftUpsertRequest body
    ) {
        apiKeyAuthorizer.verify(apiKey);
        long chatId = parseTelegramChatId(telegramChatIdHeader);
        return ResponseEntity.ok(botMeShiftsVocationsService.updateShift(chatId, shiftId, body));
    }

    @PostMapping("/shifts/{shiftId}/end-time")
    public ResponseEntity<BotShiftItem> setShiftEndTime(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader,
            @PathVariable("shiftId") UUID shiftId,
            @RequestBody(required = false) BotShiftEndTimeRequest body
    ) {
        apiKeyAuthorizer.verify(apiKey);
        long chatId = parseTelegramChatId(telegramChatIdHeader);
        return ResponseEntity.ok(botMeShiftsVocationsService.setShiftEndTime(chatId, shiftId, body));
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
