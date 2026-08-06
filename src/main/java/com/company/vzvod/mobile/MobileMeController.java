package com.company.vzvod.mobile;

import com.company.vzvod.bot.BotMeEventsService;
import com.company.vzvod.bot.BotMeExtrasService;
import com.company.vzvod.bot.BotMeProfileService;
import com.company.vzvod.bot.BotMeShiftsVocationsService;
import com.company.vzvod.bot.dto.BotAdministrativeViolationCreateRequest;
import com.company.vzvod.bot.dto.BotColleaguesResponse;
import com.company.vzvod.bot.dto.BotCriminalViolationCreateRequest;
import com.company.vzvod.bot.dto.BotEducationDto;
import com.company.vzvod.bot.dto.BotEducationUpsertRequest;
import com.company.vzvod.bot.dto.BotEventsResponse;
import com.company.vzvod.bot.dto.BotProfilePatchRequest;
import com.company.vzvod.bot.dto.BotProfileResponse;
import com.company.vzvod.bot.dto.BotShiftEndTimeRequest;
import com.company.vzvod.bot.dto.BotShiftItem;
import com.company.vzvod.bot.dto.BotShiftMetricDeltaRequest;
import com.company.vzvod.bot.dto.BotShiftUpsertRequest;
import com.company.vzvod.bot.dto.BotShiftsResponse;
import com.company.vzvod.bot.dto.BotVacationsResponse;
import com.company.vzvod.bot.dto.BotVehicleItem;
import com.company.vzvod.bot.dto.BotVehicleUpsertRequest;
import com.company.vzvod.bot.dto.BotVehiclesResponse;
import com.company.vzvod.bot.dto.BotViolationOptionsResponse;
import com.company.vzvod.bot.dto.BotVocationCreateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/mobile/me")
public class MobileMeController {

    private final MobileAuthService mobileAuthService;
    private final BotMeProfileService profileService;
    private final BotMeShiftsVocationsService shiftsService;
    private final BotMeEventsService eventsService;
    private final BotMeExtrasService extrasService;

    public MobileMeController(
            MobileAuthService mobileAuthService,
            BotMeProfileService profileService,
            BotMeShiftsVocationsService shiftsService,
            BotMeEventsService eventsService,
            BotMeExtrasService extrasService
    ) {
        this.mobileAuthService = mobileAuthService;
        this.profileService = profileService;
        this.shiftsService = shiftsService;
        this.eventsService = eventsService;
        this.extrasService = extrasService;
    }

    @GetMapping("/profile")
    public ResponseEntity<BotProfileResponse> profile(@RequestHeader(value = "X-Mobile-Token", required = false) String token) {
        return ResponseEntity.ok(profileService.loadProfile(userId(token)));
    }

    @PutMapping("/profile")
    public ResponseEntity<BotProfileResponse> updateProfile(
            @RequestHeader(value = "X-Mobile-Token", required = false) String token,
            @RequestBody(required = false) BotProfilePatchRequest body
    ) {
        return ResponseEntity.ok(profileService.updateProfile(userId(token), body));
    }

    @GetMapping("/education")
    public ResponseEntity<BotEducationDto> education(@RequestHeader(value = "X-Mobile-Token", required = false) String token) {
        BotEducationDto dto = extrasService.loadEducation(userId(token));
        return dto == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(dto);
    }

    @PutMapping("/education")
    public ResponseEntity<BotEducationDto> upsertEducation(
            @RequestHeader(value = "X-Mobile-Token", required = false) String token,
            @RequestBody(required = false) BotEducationUpsertRequest body
    ) {
        return ResponseEntity.ok(extrasService.upsertEducation(userId(token), body));
    }

    @GetMapping("/vehicles")
    public ResponseEntity<BotVehiclesResponse> vehicles(@RequestHeader(value = "X-Mobile-Token", required = false) String token) {
        return ResponseEntity.ok(extrasService.loadVehicles(userId(token)));
    }

    @PostMapping("/vehicles")
    public ResponseEntity<BotVehicleItem> createVehicle(
            @RequestHeader(value = "X-Mobile-Token", required = false) String token,
            @RequestBody(required = false) BotVehicleUpsertRequest body
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(extrasService.createVehicle(userId(token), body));
    }

    @PutMapping("/vehicles/{vehicleId}")
    public ResponseEntity<BotVehicleItem> updateVehicle(
            @RequestHeader(value = "X-Mobile-Token", required = false) String token,
            @PathVariable UUID vehicleId,
            @RequestBody(required = false) BotVehicleUpsertRequest body
    ) {
        return ResponseEntity.ok(extrasService.updateVehicle(userId(token), vehicleId, body));
    }

    @DeleteMapping("/vehicles/{vehicleId}")
    public ResponseEntity<Void> deleteVehicle(
            @RequestHeader(value = "X-Mobile-Token", required = false) String token,
            @PathVariable UUID vehicleId
    ) {
        extrasService.deleteVehicle(userId(token), vehicleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/shifts")
    public ResponseEntity<BotShiftsResponse> shifts(@RequestHeader(value = "X-Mobile-Token", required = false) String token) {
        return ResponseEntity.ok(shiftsService.loadShifts(userId(token)));
    }

    @GetMapping("/shifts/{shiftId}")
    public ResponseEntity<BotShiftItem> shift(
            @RequestHeader(value = "X-Mobile-Token", required = false) String token,
            @PathVariable UUID shiftId
    ) {
        return ResponseEntity.ok(shiftsService.loadShift(userId(token), shiftId));
    }

    @PostMapping("/shifts")
    public ResponseEntity<BotShiftItem> createShift(
            @RequestHeader(value = "X-Mobile-Token", required = false) String token,
            @RequestBody(required = false) BotShiftUpsertRequest body
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shiftsService.createShift(userId(token), body));
    }

    @PutMapping("/shifts/{shiftId}")
    public ResponseEntity<BotShiftItem> updateShift(
            @RequestHeader(value = "X-Mobile-Token", required = false) String token,
            @PathVariable UUID shiftId,
            @RequestBody(required = false) BotShiftUpsertRequest body
    ) {
        return ResponseEntity.ok(shiftsService.updateShift(userId(token), shiftId, body));
    }

    @PostMapping("/shifts/{shiftId}/end-time")
    public ResponseEntity<BotShiftItem> endShift(
            @RequestHeader(value = "X-Mobile-Token", required = false) String token,
            @PathVariable UUID shiftId,
            @RequestBody(required = false) BotShiftEndTimeRequest body
    ) {
        return ResponseEntity.ok(shiftsService.setShiftEndTime(userId(token), shiftId, body));
    }

    /** ИБДР ± → ibdWithoutMigrant */
    @PostMapping("/shifts/{shiftId}/ibd-without-migrant")
    public ResponseEntity<BotShiftItem> adjustIbdWithout(
            @RequestHeader(value = "X-Mobile-Token", required = false) String token,
            @PathVariable UUID shiftId,
            @RequestBody(required = false) BotShiftMetricDeltaRequest body
    ) {
        return ResponseEntity.ok(shiftsService.adjustIbdWithoutMigrant(userId(token), shiftId, body));
    }

    /** Мигрант ± → оба счётчика IBD */
    @PostMapping("/shifts/{shiftId}/migrant-check")
    public ResponseEntity<BotShiftItem> adjustMigrant(
            @RequestHeader(value = "X-Mobile-Token", required = false) String token,
            @PathVariable UUID shiftId,
            @RequestBody(required = false) BotShiftMetricDeltaRequest body
    ) {
        return ResponseEntity.ok(shiftsService.adjustMigrantCheck(userId(token), shiftId, body));
    }

    @PostMapping("/shifts/{shiftId}/ibd-with-migrant")
    public ResponseEntity<BotShiftItem> adjustIbdWith(
            @RequestHeader(value = "X-Mobile-Token", required = false) String token,
            @PathVariable UUID shiftId,
            @RequestBody(required = false) BotShiftMetricDeltaRequest body
    ) {
        return ResponseEntity.ok(shiftsService.adjustIbdWithMigrant(userId(token), shiftId, body));
    }

    @PostMapping("/shifts/{shiftId}/count-of-statements")
    public ResponseEntity<BotShiftItem> adjustStatements(
            @RequestHeader(value = "X-Mobile-Token", required = false) String token,
            @PathVariable UUID shiftId,
            @RequestBody(required = false) BotShiftMetricDeltaRequest body
    ) {
        return ResponseEntity.ok(shiftsService.adjustCountOfStatements(userId(token), shiftId, body));
    }

    @PostMapping("/shifts/{shiftId}/count-of-claims")
    public ResponseEntity<BotShiftItem> adjustClaims(
            @RequestHeader(value = "X-Mobile-Token", required = false) String token,
            @PathVariable UUID shiftId,
            @RequestBody(required = false) BotShiftMetricDeltaRequest body
    ) {
        return ResponseEntity.ok(shiftsService.adjustCountOfClaims(userId(token), shiftId, body));
    }

    @GetMapping("/violation-options")
    public ResponseEntity<BotViolationOptionsResponse> violationOptions(
            @RequestHeader(value = "X-Mobile-Token", required = false) String token
    ) {
        userId(token);
        return ResponseEntity.ok(shiftsService.loadViolationOptions());
    }

    @PostMapping("/shifts/{shiftId}/administrative-violations")
    public ResponseEntity<BotShiftItem> createAdmin(
            @RequestHeader(value = "X-Mobile-Token", required = false) String token,
            @PathVariable UUID shiftId,
            @RequestBody(required = false) BotAdministrativeViolationCreateRequest body
    ) {
        return ResponseEntity.ok(shiftsService.createAdministrativeViolation(userId(token), shiftId, body));
    }

    @PostMapping("/shifts/{shiftId}/criminal-violations")
    public ResponseEntity<BotShiftItem> createCriminal(
            @RequestHeader(value = "X-Mobile-Token", required = false) String token,
            @PathVariable UUID shiftId,
            @RequestBody(required = false) BotCriminalViolationCreateRequest body
    ) {
        return ResponseEntity.ok(shiftsService.createCriminalViolation(userId(token), shiftId, body));
    }

    @GetMapping("/colleagues")
    public ResponseEntity<BotColleaguesResponse> colleagues(
            @RequestHeader(value = "X-Mobile-Token", required = false) String token,
            @RequestParam("department") int department,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "excludeShiftId", required = false) UUID excludeShiftId
    ) {
        return ResponseEntity.ok(shiftsService.loadColleagues(userId(token), department, page, excludeShiftId));
    }

    @GetMapping("/vacations")
    public ResponseEntity<BotVacationsResponse> vacations(@RequestHeader(value = "X-Mobile-Token", required = false) String token) {
        return ResponseEntity.ok(shiftsService.loadVacations(userId(token)));
    }

    @PostMapping("/vacations")
    public ResponseEntity<BotVacationsResponse> createVacation(
            @RequestHeader(value = "X-Mobile-Token", required = false) String token,
            @RequestBody(required = false) BotVocationCreateRequest body
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shiftsService.createVocation(userId(token), body));
    }

    @DeleteMapping("/vacations/{vocationId}")
    public ResponseEntity<BotVacationsResponse> deleteVacation(
            @RequestHeader(value = "X-Mobile-Token", required = false) String token,
            @PathVariable UUID vocationId
    ) {
        return ResponseEntity.ok(shiftsService.deleteVocation(userId(token), vocationId));
    }

    @GetMapping("/events")
    public ResponseEntity<BotEventsResponse> events(@RequestHeader(value = "X-Mobile-Token", required = false) String token) {
        return ResponseEntity.ok(eventsService.loadUpcomingEvents(userId(token)));
    }

    private UUID userId(String token) {
        UUID id = mobileAuthService.requireActiveUserIdByToken(token);
        mobileAuthService.touchLastSeen(token);
        return id;
    }
}
