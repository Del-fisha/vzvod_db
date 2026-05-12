package com.company.vzvod.bot;

import com.company.vzvod.bot.dto.BotShiftItem;
import com.company.vzvod.bot.dto.BotShiftsResponse;
import com.company.vzvod.bot.dto.BotVacationBalance;
import com.company.vzvod.bot.dto.BotVacationsResponse;
import com.company.vzvod.bot.dto.BotVocationItem;
import com.company.vzvod.entity.NumberOfShift;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.Shift;
import com.company.vzvod.entity.TypeOfShift;
import com.company.vzvod.entity.User;
import com.company.vzvod.entity.UserTelegramBinding;
import com.company.vzvod.entity.Vocation;
import com.company.vzvod.entity.VocationType;
import com.company.vzvod.entity.Dep;
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
public class BotMeShiftsVocationsService {

    private static final Locale RU = Locale.forLanguageTag("ru");
    private static final int MAX_SHIFTS = 35;
    private static final int MAX_VOCATIONS = 30;

    private final UnconstrainedDataManager unconstrainedDataManager;
    private final BotActiveUserChecker activeUserChecker;
    private final MessageSource messageSource;

    public BotMeShiftsVocationsService(
            UnconstrainedDataManager unconstrainedDataManager,
            BotActiveUserChecker activeUserChecker,
            MessageSource messageSource
    ) {
        this.unconstrainedDataManager = unconstrainedDataManager;
        this.activeUserChecker = activeUserChecker;
        this.messageSource = messageSource;
    }

    @Transactional(readOnly = true)
    public BotShiftsResponse loadShifts(long telegramChatId) {
        UUID serviceInfoId = loadServiceInfoId(telegramChatId);
        List<Shift> shifts = unconstrainedDataManager.load(Shift.class)
                .query("select distinct s from Shift s join s.units u where u.id = :sid order by s.date desc, s.startTime desc")
                .parameter("sid", serviceInfoId)
                .maxResults(MAX_SHIFTS)
                .fetchPlan(f -> f
                        .add("date")
                        .add("number")
                        .add("typeOfShift")
                        .add("departmentToday")
                        .add("startTime")
                        .add("endTime"))
                .list();
        List<BotShiftItem> items = new ArrayList<>(shifts.size());
        for (Shift s : shifts) {
            NumberOfShift route = s.getNumber();
            TypeOfShift type = s.getTypeOfShift();
            Dep dep = s.getDepartmentToday();
            items.add(new BotShiftItem(
                    s.getDate(),
                    route == null ? null : route.getId(),
                    type == null ? null : enumMessage("TypeOfShift", type.name()),
                    dep == null ? null : enumMessage("Dep", dep.name()),
                    s.getStartTime(),
                    s.getEndTime()
            ));
        }
        return new BotShiftsResponse(items);
    }

    @Transactional(readOnly = true)
    public BotVacationsResponse loadVacations(long telegramChatId) {
        User user = loadUserWithVacationFields(telegramChatId);
        ServiceInfo si = user.getServiceInfo();
        if (si == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "service info missing");
        }
        int entitled = si.getVacationDaysEntitled() == null ? 0 : si.getVacationDaysEntitled();
        int available = si.getVacationDaysAvailable() == null ? 0 : si.getVacationDaysAvailable();
        int used = Math.max(0, entitled - available);
        BotVacationBalance balance = new BotVacationBalance(entitled, available, used);

        List<Vocation> vocations = unconstrainedDataManager.load(Vocation.class)
                .query("select v from Vocation v where v.userServiceInfo.id = :sid order by v.startDate desc")
                .parameter("sid", si.getId())
                .maxResults(MAX_VOCATIONS)
                .list();
        List<BotVocationItem> items = new ArrayList<>(vocations.size());
        for (Vocation v : vocations) {
            VocationType t = v.getType();
            String typeLabel = t == null ? null : enumMessage("VocationType", t.name());
            items.add(new BotVocationItem(
                    v.getStartDate(),
                    v.getEndDate(),
                    v.getCountOfDays(),
                    typeLabel
            ));
        }
        return new BotVacationsResponse(balance, items);
    }

    private UUID loadServiceInfoId(long telegramChatId) {
        User user = loadUserWithVacationFields(telegramChatId);
        ServiceInfo si = user.getServiceInfo();
        if (si == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "service info missing");
        }
        return si.getId();
    }

    private User loadUserWithVacationFields(long telegramChatId) {
        UserTelegramBinding binding = unconstrainedDataManager.load(UserTelegramBinding.class)
                .query("select b from UserTelegramBinding b where b.chatId = :cid")
                .parameter("cid", telegramChatId)
                .optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "telegram chat not bound"));

        UUID userId = binding.getUser().getId();
        activeUserChecker.requireActive(userId);

        return unconstrainedDataManager.load(User.class)
                .id(userId)
                .fetchPlan(fp -> fp.add("serviceInfo", sf -> sf
                        .add("vacationDaysEntitled")
                        .add("vacationDaysAvailable")))
                .one();
    }

    private String enumMessage(String enumSimpleName, String enumConstantName) {
        String code = "com.company.vzvod.entity/" + enumSimpleName + "." + enumConstantName;
        try {
            return messageSource.getMessage(code, null, RU);
        } catch (NoSuchMessageException e) {
            return enumConstantName;
        }
    }
}
