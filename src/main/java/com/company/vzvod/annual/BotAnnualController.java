package com.company.vzvod.annual;

import com.company.vzvod.annual.AnnualDtos.AnnualBoardDto;
import com.company.vzvod.annual.AnnualDtos.AnnualFlagsRequest;
import com.company.vzvod.annual.AnnualDtos.AnnualRosterDto;
import com.company.vzvod.bot.BotApiKeyAuthorizer;
import com.company.vzvod.bot.BotTelegramBindingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/bot/me/annual")
public class BotAnnualController {

    private final BotApiKeyAuthorizer apiKeyAuthorizer;
    private final BotTelegramBindingService telegramBindingService;
    private final AnnualBoardService annualBoardService;

    public BotAnnualController(
            BotApiKeyAuthorizer apiKeyAuthorizer,
            BotTelegramBindingService telegramBindingService,
            AnnualBoardService annualBoardService
    ) {
        this.apiKeyAuthorizer = apiKeyAuthorizer;
        this.telegramBindingService = telegramBindingService;
        this.annualBoardService = annualBoardService;
    }

    @GetMapping
    public ResponseEntity<AnnualBoardDto> board(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader
    ) {
        apiKeyAuthorizer.verify(apiKey);
        return ResponseEntity.ok(annualBoardService.loadOwn(resolveUserId(telegramChatIdHeader)));
    }

    @PutMapping("/flags")
    public ResponseEntity<AnnualBoardDto> flags(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader,
            @RequestBody AnnualFlagsRequest body
    ) {
        apiKeyAuthorizer.verify(apiKey);
        return ResponseEntity.ok(annualBoardService.setFlags(resolveUserId(telegramChatIdHeader), body));
    }

    @GetMapping("/others")
    public ResponseEntity<AnnualRosterDto> others(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Telegram-Chat-Id", required = false) String telegramChatIdHeader
    ) {
        apiKeyAuthorizer.verify(apiKey);
        return ResponseEntity.ok(annualBoardService.roster(
                resolveUserId(telegramChatIdHeader),
                AnnualSection.ALL,
                null,
                AnnualSort.DEFAULT
        ));
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
