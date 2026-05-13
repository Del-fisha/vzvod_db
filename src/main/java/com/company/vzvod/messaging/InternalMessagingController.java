package com.company.vzvod.messaging;

import com.company.vzvod.messaging.dto.MessagingDeliveryTargetDto;
import com.company.vzvod.messaging.dto.MessagingDeliveryTargetRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/internal/messaging")
public class InternalMessagingController {

    private static final Logger log = LoggerFactory.getLogger(InternalMessagingController.class);

    private final MessagingDeliveryTargetService deliveryTargetService;
    private final String internalToken;

    public InternalMessagingController(
            MessagingDeliveryTargetService deliveryTargetService,
            @Value("${internal.api.token:}") String internalToken
    ) {
        this.deliveryTargetService = deliveryTargetService;
        this.internalToken = internalToken;
    }

    @PostMapping("/telegram-targets")
    public List<MessagingDeliveryTargetDto> resolveTelegramTargets(
            @RequestHeader(name = "X-Internal-Token", required = false) String token,
            @RequestBody MessagingDeliveryTargetRequestDto request
    ) {
        requireInternalToken(token);
        if (request == null || request.recipientUserIds() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        log.info("Resolve telegram targets: recipients={}", request.recipientUserIds().size());
        return deliveryTargetService.resolveTelegramTargets(request.recipientUserIds());
    }

    private void requireInternalToken(String token) {
        if (internalToken == null || internalToken.isBlank()) {
            return;
        }
        if (token == null || !internalToken.equals(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
}
