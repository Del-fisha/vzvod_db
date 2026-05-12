package com.company.vzvod.bot;

import com.company.vzvod.bot.dto.BotProfilePatchRequest;
import com.company.vzvod.bot.dto.BotProfileResponse;
import com.company.vzvod.entity.Department;
import com.company.vzvod.entity.IdCard;
import com.company.vzvod.entity.Post;
import com.company.vzvod.entity.Rank;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.User;
import com.company.vzvod.entity.UserTelegramBinding;
import io.jmix.core.UnconstrainedDataManager;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.UUID;

@Service
public class BotMeProfileService {

    private static final Locale RU = Locale.forLanguageTag("ru");

    private final UnconstrainedDataManager unconstrainedDataManager;
    private final BotActiveUserChecker activeUserChecker;
    private final MessageSource messageSource;

    public BotMeProfileService(
            UnconstrainedDataManager unconstrainedDataManager,
            BotActiveUserChecker activeUserChecker,
            MessageSource messageSource
    ) {
        this.unconstrainedDataManager = unconstrainedDataManager;
        this.activeUserChecker = activeUserChecker;
        this.messageSource = messageSource;
    }

    @Transactional(readOnly = true)
    public BotProfileResponse loadProfile(long telegramChatId) {
        User user = loadActiveUserForChat(telegramChatId);
        return toResponse(user);
    }

    @Transactional
    public BotProfileResponse updateProfile(long telegramChatId, BotProfilePatchRequest patch) {
        if (patch == null || !patch.hasAnyField()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no fields to update");
        }
        User user = loadActiveUserForChat(telegramChatId);
        ServiceInfo si = user.getServiceInfo();
        if (si == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "service info missing");
        }
        if (patch.breastplate() != null) {
            String bp = patch.breastplate().trim();
            if (bp.length() != 8) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "breastplate must be exactly 8 characters");
            }
            si.setBreastplate(bp);
        }
        if (patch.medicalExamination() != null) {
            si.setMedicalExamination(patch.medicalExamination());
        }
        unconstrainedDataManager.save(si);

        User refreshed = unconstrainedDataManager.load(User.class)
                .id(user.getId())
                .fetchPlan(fp -> fp
                        .add("firstName")
                        .add("lastName")
                        .add("patronymic")
                        .add("serviceInfo", sf -> sf
                                .add("status")
                                .add("department", d -> d.add("number"))
                                .add("idCard", ic -> ic.add("issued").add("until").add("spl"))
                                .add("breastplate")
                                .add("medicalExamination")
                                .add("rank")
                                .add("post")))
                .one();
        return toResponse(refreshed);
    }

    private User loadActiveUserForChat(long telegramChatId) {
        UserTelegramBinding binding = unconstrainedDataManager.load(UserTelegramBinding.class)
                .query("select b from UserTelegramBinding b where b.chatId = :cid")
                .parameter("cid", telegramChatId)
                .optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "telegram chat not bound"));

        UUID userId = binding.getUser().getId();
        activeUserChecker.requireActive(userId);

        return unconstrainedDataManager.load(User.class)
                .id(userId)
                .fetchPlan(fp -> fp
                        .add("firstName")
                        .add("lastName")
                        .add("patronymic")
                        .add("serviceInfo", sf -> sf
                                .add("status")
                                .add("department", d -> d.add("number"))
                                .add("idCard", ic -> ic.add("issued").add("until").add("spl"))
                                .add("breastplate")
                                .add("medicalExamination")
                                .add("rank")
                                .add("post")))
                .one();
    }

    private BotProfileResponse toResponse(User user) {
        ServiceInfo si = user.getServiceInfo();
        if (si == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "service info missing");
        }
        Rank rank = si.getRank();
        Post post = si.getPost();
        Department dept = si.getDepartment();
        IdCard card = si.getIdCard();
        Boolean med = si.getMedicalExamination();
        boolean medical = med != null && med;
        return new BotProfileResponse(
                user.getId(),
                user.getDisplayName(),
                rankMessage(rank),
                postMessage(post),
                departmentLabel(dept),
                si.getBreastplate(),
                medical,
                card != null ? card.getIssued() : null,
                card != null ? card.getUntil() : null
        );
    }

    private String rankMessage(Rank rank) {
        if (rank == null) {
            return null;
        }
        return enumMessage("Rank", rank.name());
    }

    private String postMessage(Post post) {
        if (post == null) {
            return null;
        }
        return enumMessage("Post", post.name());
    }

    private String enumMessage(String enumSimpleName, String enumConstantName) {
        String code = "com.company.vzvod.entity/" + enumSimpleName + "." + enumConstantName;
        try {
            return messageSource.getMessage(code, null, RU);
        } catch (NoSuchMessageException e) {
            return enumConstantName;
        }
    }

    private static String departmentLabel(Department department) {
        if (department == null || department.getNumber() == null) {
            return null;
        }
        return "Отделение № " + department.getNumber();
    }
}
