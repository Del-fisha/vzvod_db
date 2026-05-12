package com.company.vzvod.bot;

import com.company.vzvod.bot.dto.BotAuthResponse;
import com.company.vzvod.entity.Contacts;
import com.company.vzvod.entity.User;
import com.company.vzvod.entity.UserTelegramBinding;
import com.company.vzvod.service.PhoneNormalizer;
import io.jmix.core.UnconstrainedDataManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class BotAuthService {

    private static final Pattern RU_MOBILE_E164 = Pattern.compile("^\\+7\\d{10}$");

    private final UnconstrainedDataManager unconstrainedDataManager;
    private final BotActiveUserChecker activeUserChecker;

    public BotAuthService(UnconstrainedDataManager unconstrainedDataManager, BotActiveUserChecker activeUserChecker) {
        this.unconstrainedDataManager = unconstrainedDataManager;
        this.activeUserChecker = activeUserChecker;
    }

    @Transactional
    public BotAuthResponse authenticate(String phoneRaw, long chatId) {
        String normalized = normalizeRussianMobileOrThrow(phoneRaw);

        Contacts contacts = findContactsByNormalizedPhone(normalized);
        if (contacts == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "phone not found");
        }

        User user = unconstrainedDataManager.load(User.class)
                .id(contacts.getUser().getId())
                .fetchPlan(fp -> fp
                        .add("firstName")
                        .add("lastName")
                        .add("patronymic"))
                .one();

        activeUserChecker.requireActive(user.getId());

        upsertBinding(user, chatId);
        return new BotAuthResponse(user.getId(), user.getDisplayName());
    }

    /**
     * Сравнение по расшифрованному номеру: JPQL {@code where c.phoneNumber = :p} не применяет конвертер к параметру,
     * а шифротекст в БД недетерминирован (AES-GCM), поэтому ищем среди контактов в памяти.
     * Для размера взвода это приемлемо; при масштабировании — отдельный детерминированный индекс (HMAC и т.п.).
     */
    private Contacts findContactsByNormalizedPhone(String normalizedPhone) {
        List<Contacts> all = unconstrainedDataManager.load(Contacts.class)
                .query("select c from Contacts c join fetch c.user join fetch c.user.serviceInfo")
                .list();
        for (Contacts c : all) {
            if (normalizedPhone.equals(c.getPhoneNumber())) {
                return c;
            }
        }
        return null;
    }

    private void upsertBinding(User user, long chatId) {
        UserTelegramBinding forChat = unconstrainedDataManager.load(UserTelegramBinding.class)
                .query("select b from UserTelegramBinding b where b.chatId = :cid")
                .parameter("cid", chatId)
                .optional()
                .orElse(null);
        if (forChat != null && !forChat.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "chat already bound to another user");
        }

        UserTelegramBinding forUser = unconstrainedDataManager.load(UserTelegramBinding.class)
                .query("select b from UserTelegramBinding b where b.user.id = :uid")
                .parameter("uid", user.getId())
                .optional()
                .orElse(null);

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (forUser == null) {
            UserTelegramBinding b = unconstrainedDataManager.create(UserTelegramBinding.class);
            b.setUser(user);
            b.setChatId(chatId);
            b.setRegisteredAt(now);
            unconstrainedDataManager.save(b);
        } else {
            forUser.setChatId(chatId);
            forUser.setRegisteredAt(now);
            unconstrainedDataManager.save(forUser);
        }
    }

    private static String normalizeRussianMobileOrThrow(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("phoneNumber is required");
        }
        String trimmed = raw.trim().replace(" ", "");
        PhoneNormalizer normalizer = new PhoneNormalizer();
        String n;
        try {
            n = normalizer.normalize(trimmed);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("invalid phone number format");
        }
        if (!RU_MOBILE_E164.matcher(n).matches()) {
            throw new IllegalArgumentException("invalid phone number format");
        }
        return n;
    }
}
