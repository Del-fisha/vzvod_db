package com.company.vzvod.annual;

import com.company.vzvod.annual.AnnualDtos.AnnualBoardDto;
import com.company.vzvod.annual.AnnualDtos.AnnualPatchRequest;
import com.company.vzvod.annual.AnnualDtos.AnnualRosterDto;
import com.company.vzvod.mobile.MobileAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/mobile/me/annual")
public class MobileAnnualController {

    private final MobileAuthService mobileAuthService;
    private final AnnualBoardService annualBoardService;

    public MobileAnnualController(MobileAuthService mobileAuthService, AnnualBoardService annualBoardService) {
        this.mobileAuthService = mobileAuthService;
        this.annualBoardService = annualBoardService;
    }

    @GetMapping
    public ResponseEntity<AnnualBoardDto> board(@RequestHeader(value = "X-Mobile-Token", required = false) String token) {
        return ResponseEntity.ok(annualBoardService.loadOwn(userId(token)));
    }

    @PutMapping
    public ResponseEntity<AnnualBoardDto> patch(
            @RequestHeader(value = "X-Mobile-Token", required = false) String token,
            @RequestBody AnnualPatchRequest body
    ) {
        return ResponseEntity.ok(annualBoardService.patchOwn(userId(token), body));
    }

    @GetMapping("/others")
    public ResponseEntity<AnnualRosterDto> others(
            @RequestHeader(value = "X-Mobile-Token", required = false) String token,
            @RequestParam(value = "section", required = false) String section,
            @RequestParam(value = "department", required = false) Integer department,
            @RequestParam(value = "sort", required = false) String sort
    ) {
        return ResponseEntity.ok(annualBoardService.roster(
                userId(token),
                AnnualSection.fromId(section),
                department,
                AnnualSort.fromId(sort)
        ));
    }

    private UUID userId(String token) {
        UUID id = mobileAuthService.requireActiveUserIdByToken(token);
        mobileAuthService.touchLastSeen(token);
        return id;
    }
}
