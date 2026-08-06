package com.company.vzvod.mobile;

import com.company.vzvod.mobile.dto.MobileAuthRequest;
import com.company.vzvod.mobile.dto.MobileAuthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mobile")
public class MobileAuthController {

    private final MobileAuthService mobileAuthService;

    public MobileAuthController(MobileAuthService mobileAuthService) {
        this.mobileAuthService = mobileAuthService;
    }

    @PostMapping("/auth")
    public ResponseEntity<MobileAuthResponse> auth(@RequestBody(required = false) MobileAuthRequest body) {
        return ResponseEntity.ok(mobileAuthService.authenticate(body));
    }
}
