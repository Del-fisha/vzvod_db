package com.company.vzvod.bot;

import com.company.vzvod.bot.dto.BotAdministrativeViolationCreateRequest;
import com.company.vzvod.bot.dto.BotCatalogOptionsResponse;
import com.company.vzvod.bot.dto.BotEventCreateRequest;
import com.company.vzvod.bot.dto.BotEventItem;
import com.company.vzvod.bot.dto.BotEventsResponse;
import com.company.vzvod.bot.dto.BotProfilePatchRequest;
import com.company.vzvod.bot.dto.BotProfileResponse;
import com.company.vzvod.bot.dto.BotColleaguesResponse;
import com.company.vzvod.bot.dto.BotCriminalViolationCreateRequest;
import com.company.vzvod.bot.dto.BotShiftEndTimeRequest;
import com.company.vzvod.bot.dto.BotShiftItem;
import com.company.vzvod.bot.dto.BotShiftMetricDeltaRequest;
import com.company.vzvod.bot.dto.BotShiftUpsertRequest;
import com.company.vzvod.bot.dto.BotShiftsResponse;
import com.company.vzvod.bot.dto.BotVacationsResponse;
import com.company.vzvod.bot.dto.BotViolationOptionsResponse;
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
    private final BotTelegramBindingService telegramBindingService;
    private final BotMeProfileService botMeProfileService;
    private final BotMeShiftsVocationsService botMeShiftsVocationsService;
    private final BotMeEventsService botMeEventsService;

    public BotMeController(
            BotApiKeyAuthorizer apiKeyAuthorizer,
            BotTelegramBindingService telegramBindingService,
            BotMeProfileService botMeProfileService,
            BotMeShiftsVocationsService botMeShiftsVocationsService,
            BotMeEventsService botMeEventsService
    ) {
        this.apiKeyAuthorizer = apiKeyAuthorizer;
        this.telegramBindingService = telegramBindingService;
        this.botMeProfileService = botMeProfileService;
        this.botMeShiftsVocationsService = botMeShiftsVocationsService;
        this.botMeEventsService = botMeEventsService;
    }

    @GetMapping("/profile")
    public ResponseEntity<BotProfileResponse> profile(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader
    ) {
        apiKeyAuthorizer.verify(apiKey);
        UUID userId = resolveUserId(telegramChatIdHeader);
        return ResponseEntity.ok(botMeProfileService.loadProfile(userId));
    }

    @GetMapping("/shifts")
    public ResponseEntity<BotShiftsResponse> shifts(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader
    ) {
        apiKeyAuthorizer.verify(apiKey);
        UUID userId = resolveUserId(telegramChatIdHeader);
        return ResponseEntity.ok(botMeShiftsVocationsService.loadShifts(userId));
    }

    @GetMapping("/colleagues")
    public ResponseEntity<BotColleaguesResponse> colleagues(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader,
            @RequestParam("department") int department,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "excludeShiftId", required = false) UUID excludeShiftId
    ) {
        apiKeyAuthorizer.verify(apiKey);
        UUID userId = resolveUserId(telegramChatIdHeader);
        return ResponseEntity.ok(botMeShiftsVocationsService.loadColleagues(userId, department, page, excludeShiftId));
    }

    @GetMapping("/vacations")
    public ResponseEntity<BotVacationsResponse> vacations(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader
    ) {
        apiKeyAuthorizer.verify(apiKey);
        UUID userId = resolveUserId(telegramChatIdHeader);
        return ResponseEntity.ok(botMeShiftsVocationsService.loadVacations(userId));
    }

    @GetMapping("/events")
    public ResponseEntity<BotEventsResponse> events(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader
    ) {
        apiKeyAuthorizer.verify(apiKey);
        UUID userId = resolveUserId(telegramChatIdHeader);
        return ResponseEntity.ok(botMeEventsService.loadUpcomingEvents(userId));
    }

    @PostMapping("/events")
    public ResponseEntity<BotEventItem> createEvent(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader,
            @RequestBody(required = false) BotEventCreateRequest body
    ) {
        apiKeyAuthorizer.verify(apiKey);
        UUID userId = resolveUserId(telegramChatIdHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(botMeEventsService.createEvent(userId, body));
    }

    @GetMapping("/shifts/{shiftId}")
    public ResponseEntity<BotShiftItem> shift(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader,
            @PathVariable("shiftId") UUID shiftId
    ) {
        apiKeyAuthorizer.verify(apiKey);
        UUID userId = resolveUserId(telegramChatIdHeader);
        return ResponseEntity.ok(botMeShiftsVocationsService.loadShift(userId, shiftId));
    }

    @PostMapping("/shifts")
    public ResponseEntity<BotShiftItem> createShift(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader,
            @RequestBody(required = false) BotShiftUpsertRequest body
    ) {
        apiKeyAuthorizer.verify(apiKey);
        UUID userId = resolveUserId(telegramChatIdHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(botMeShiftsVocationsService.createShift(userId, body));
    }

    @PutMapping("/shifts/{shiftId}")
    public ResponseEntity<BotShiftItem> updateShift(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader,
            @PathVariable("shiftId") UUID shiftId,
            @RequestBody(required = false) BotShiftUpsertRequest body
    ) {
        apiKeyAuthorizer.verify(apiKey);
        UUID userId = resolveUserId(telegramChatIdHeader);
        return ResponseEntity.ok(botMeShiftsVocationsService.updateShift(userId, shiftId, body));
    }

    @PostMapping("/shifts/{shiftId}/end-time")
    public ResponseEntity<BotShiftItem> setShiftEndTime(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader,
            @PathVariable("shiftId") UUID shiftId,
            @RequestBody(required = false) BotShiftEndTimeRequest body
    ) {
        apiKeyAuthorizer.verify(apiKey);
        UUID userId = resolveUserId(telegramChatIdHeader);
        return ResponseEntity.ok(botMeShiftsVocationsService.setShiftEndTime(userId, shiftId, body));
    }

    @PostMapping("/shifts/{shiftId}/ibdr")
    public ResponseEntity<BotShiftItem> adjustIbdr(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader,
            @PathVariable("shiftId") UUID shiftId,
            @RequestBody(required = false) BotShiftMetricDeltaRequest body
    ) {
        apiKeyAuthorizer.verify(apiKey);
        UUID userId = resolveUserId(telegramChatIdHeader);
        return ResponseEntity.ok(botMeShiftsVocationsService.adjustIbdr(userId, shiftId, body));
    }

    @PostMapping("/shifts/{shiftId}/migrant")
    public ResponseEntity<BotShiftItem> adjustMigrant(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader,
            @PathVariable("shiftId") UUID shiftId,
            @RequestBody(required = false) BotShiftMetricDeltaRequest body
    ) {
        apiKeyAuthorizer.verify(apiKey);
        UUID userId = resolveUserId(telegramChatIdHeader);
        return ResponseEntity.ok(botMeShiftsVocationsService.adjustMigrant(userId, shiftId, body));
    }

    @PostMapping("/shifts/{shiftId}/migrant-and-ibdr")
    public ResponseEntity<BotShiftItem> adjustMigrantAndIbdr(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader,
            @PathVariable("shiftId") UUID shiftId,
            @RequestBody(required = false) BotShiftMetricDeltaRequest body
    ) {
        apiKeyAuthorizer.verify(apiKey);
        UUID userId = resolveUserId(telegramChatIdHeader);
        return ResponseEntity.ok(botMeShiftsVocationsService.adjustMigrantAndIbdr(userId, shiftId, body));
    }

    @PostMapping("/shifts/{shiftId}/count-of-statements")
    public ResponseEntity<BotShiftItem> adjustCountOfStatements(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader,
            @PathVariable("shiftId") UUID shiftId,
            @RequestBody(required = false) BotShiftMetricDeltaRequest body
    ) {
        apiKeyAuthorizer.verify(apiKey);
        UUID userId = resolveUserId(telegramChatIdHeader);
        return ResponseEntity.ok(botMeShiftsVocationsService.adjustCountOfStatements(userId, shiftId, body));
    }

    @GetMapping("/violation-options")
    public ResponseEntity<BotViolationOptionsResponse> violationOptions(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey
    ) {
        apiKeyAuthorizer.verify(apiKey);
        return ResponseEntity.ok(botMeShiftsVocationsService.loadViolationOptions());
    }

    @GetMapping("/catalog-options")
    public ResponseEntity<BotCatalogOptionsResponse> catalogOptions(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey
    ) {
        apiKeyAuthorizer.verify(apiKey);
        return ResponseEntity.ok(botMeShiftsVocationsService.loadCatalogOptions());
    }

    @PostMapping("/shifts/{shiftId}/administrative-violations")
    public ResponseEntity<BotShiftItem> createAdministrativeViolation(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader,
            @PathVariable("shiftId") UUID shiftId,
            @RequestBody(required = false) BotAdministrativeViolationCreateRequest body
    ) {
        apiKeyAuthorizer.verify(apiKey);
        UUID userId = resolveUserId(telegramChatIdHeader);
        return ResponseEntity.ok(botMeShiftsVocationsService.createAdministrativeViolation(userId, shiftId, body));
    }

    @PostMapping("/shifts/{shiftId}/criminal-violations")
    public ResponseEntity<BotShiftItem> createCriminalViolation(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader,
            @PathVariable("shiftId") UUID shiftId,
            @RequestBody(required = false) BotCriminalViolationCreateRequest body
    ) {
        apiKeyAuthorizer.verify(apiKey);
        UUID userId = resolveUserId(telegramChatIdHeader);
        return ResponseEntity.ok(botMeShiftsVocationsService.createCriminalViolation(userId, shiftId, body));
    }

    @PutMapping("/profile")
    public ResponseEntity<BotProfileResponse> updateProfile(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader,
            @RequestBody(required = false) BotProfilePatchRequest body
    ) {
        apiKeyAuthorizer.verify(apiKey);
        UUID userId = resolveUserId(telegramChatIdHeader);
        return ResponseEntity.ok(botMeProfileService.updateProfile(userId, body));
    }

    private UUID resolveUserId(String telegramChatIdHeader) {
        if (telegramChatIdHeader == null || telegramChatIdHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "X-Telegram-Chat-Id is required");
        }
        try {
            long chatId = Long.parseLong(telegramChatIdHeader.trim());
            return telegramBindingService.requireActiveUserIdByChatId(chatId);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid X-Telegram-Chat-Id");
        }
    }
}
