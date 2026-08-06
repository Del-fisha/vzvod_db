package com.company.vzvod.bot;

import com.company.vzvod.bot.dto.BotEducationDto;
import com.company.vzvod.bot.dto.BotEducationUpsertRequest;
import com.company.vzvod.bot.dto.BotVehicleItem;
import com.company.vzvod.bot.dto.BotVehicleUpsertRequest;
import com.company.vzvod.bot.dto.BotVehiclesResponse;
import com.company.vzvod.entity.Education;
import com.company.vzvod.entity.EducationStatus;
import com.company.vzvod.entity.TypeOfEducation;
import com.company.vzvod.entity.User;
import com.company.vzvod.entity.Vehicle;
import com.company.vzvod.service.EducationStatusService;
import io.jmix.core.UnconstrainedDataManager;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class BotMeExtrasService {

    private static final Locale RU = Locale.forLanguageTag("ru");

    private final UnconstrainedDataManager dataManager;
    private final BotActiveUserChecker activeUserChecker;
    private final EducationStatusService educationStatusService;
    private final MessageSource messageSource;

    public BotMeExtrasService(
            UnconstrainedDataManager dataManager,
            BotActiveUserChecker activeUserChecker,
            EducationStatusService educationStatusService,
            MessageSource messageSource
    ) {
        this.dataManager = dataManager;
        this.activeUserChecker = activeUserChecker;
        this.educationStatusService = educationStatusService;
        this.messageSource = messageSource;
    }

    @Transactional(readOnly = true)
    public BotEducationDto loadEducation(UUID userId) {
        User user = loadUser(userId);
        Education e = user.getEducation();
        if (e == null) {
            return null;
        }
        return toEducationDto(e);
    }

    @Transactional
    public BotEducationDto upsertEducation(UUID userId, BotEducationUpsertRequest body) {
        if (body == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "body required");
        }
        User user = loadUser(userId);
        Education e = user.getEducation();
        if (e == null) {
            e = dataManager.create(Education.class);
            user.setEducation(e);
        }
        e.setStarted(body.started());
        e.setUntil(body.until());
        e.setNameOfInstitution(blankToNull(body.nameOfInstitution()));
        if (body.typeId() != null && !body.typeId().isBlank()) {
            TypeOfEducation t = TypeOfEducation.fromId(body.typeId().trim());
            if (t == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown education type");
            }
            e.setType(t);
        }
        educationStatusService.applyStatusFromUntil(e);
        dataManager.save(e);
        dataManager.save(user);
        return toEducationDto(e);
    }

    @Transactional(readOnly = true)
    public BotVehiclesResponse loadVehicles(UUID userId) {
        activeUserChecker.requireActive(userId);
        List<Vehicle> list = dataManager.load(Vehicle.class)
                .query("select v from Vehicle v where v.user.id = :uid order by v.stateNumber")
                .parameter("uid", userId)
                .list();
        List<BotVehicleItem> items = new ArrayList<>(list.size());
        for (Vehicle v : list) {
            items.add(toVehicleItem(v));
        }
        return new BotVehiclesResponse(items);
    }

    @Transactional
    public BotVehicleItem createVehicle(UUID userId, BotVehicleUpsertRequest body) {
        User user = loadUser(userId);
        Vehicle v = dataManager.create(Vehicle.class);
        applyVehicle(v, body);
        v.setUser(user);
        Vehicle saved = dataManager.save(v);
        return toVehicleItem(saved);
    }

    @Transactional
    public BotVehicleItem updateVehicle(UUID userId, UUID vehicleId, BotVehicleUpsertRequest body) {
        Vehicle v = requireOwnVehicle(userId, vehicleId);
        applyVehicle(v, body);
        return toVehicleItem(dataManager.save(v));
    }

    @Transactional
    public void deleteVehicle(UUID userId, UUID vehicleId) {
        Vehicle v = requireOwnVehicle(userId, vehicleId);
        dataManager.remove(v);
    }

    private Vehicle requireOwnVehicle(UUID userId, UUID vehicleId) {
        activeUserChecker.requireActive(userId);
        Vehicle v = dataManager.load(Vehicle.class)
                .id(vehicleId)
                .optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "vehicle not found"));
        if (v.getUser() == null || !userId.equals(v.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not your vehicle");
        }
        return v;
    }

    private void applyVehicle(Vehicle v, BotVehicleUpsertRequest body) {
        if (body == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "body required");
        }
        if (body.stateNumber() == null || body.stateNumber().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "stateNumber required");
        }
        v.setStateNumber(body.stateNumber().trim().toUpperCase(Locale.ROOT));
        v.setBrand(blankToNull(body.brand()));
        v.setModel(blankToNull(body.model()));
        v.setRegistrationCertificate(blankToNull(body.registrationCertificate()));
        v.setInsurance(body.insurance());
    }

    private User loadUser(UUID userId) {
        activeUserChecker.requireActive(userId);
        return dataManager.load(User.class)
                .id(userId)
                .fetchPlan(fp -> fp.add("education").add("vehicleInfo"))
                .one();
    }

    private BotEducationDto toEducationDto(Education e) {
        TypeOfEducation t = e.getType();
        EducationStatus s = e.getStatus();
        return new BotEducationDto(
                e.getId(),
                e.getStarted(),
                e.getUntil(),
                t == null ? null : t.getId(),
                t == null ? null : enumMessage("TypeOfEducation", t.name()),
                s == null ? null : s.getId(),
                s == null ? null : enumMessage("EducationStatus", s.name()),
                e.getNameOfInstitution()
        );
    }

    private static BotVehicleItem toVehicleItem(Vehicle v) {
        return new BotVehicleItem(
                v.getId(),
                v.getStateNumber(),
                v.getBrand(),
                v.getModel(),
                v.getRegistrationCertificate(),
                v.getInsurance()
        );
    }

    private String enumMessage(String enumSimpleName, String enumConstantName) {
        String code = "com.company.vzvod.entity/" + enumSimpleName + "." + enumConstantName;
        try {
            return messageSource.getMessage(code, null, RU);
        } catch (NoSuchMessageException ex) {
            return enumConstantName;
        }
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
