package com.company.vzvod.bot;

import com.company.vzvod.bot.dto.BotAddressPatch;
import com.company.vzvod.bot.dto.BotAddressResponse;
import com.company.vzvod.bot.dto.BotProfilePatchRequest;
import com.company.vzvod.bot.dto.BotProfileResponse;
import com.company.vzvod.entity.Address;
import com.company.vzvod.entity.Contacts;
import com.company.vzvod.entity.Department;
import com.company.vzvod.entity.IdCard;
import com.company.vzvod.entity.Post;
import com.company.vzvod.entity.Rank;
import com.company.vzvod.entity.ServiceInfo;
import com.company.vzvod.entity.StatusOfHousing;
import com.company.vzvod.entity.TypeOfHousing;
import com.company.vzvod.entity.User;
import io.jmix.core.FetchPlanBuilder;
import io.jmix.core.UnconstrainedDataManager;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class BotMeProfileService {

    private static final Locale RU = Locale.forLanguageTag("ru");
    private static final Pattern INDEX_PATTERN = Pattern.compile("^\\d{6}$");

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
    public BotProfileResponse loadProfile(UUID userId) {
        User user = loadActiveUser(userId);
        return toResponse(user);
    }

    @Transactional
    public BotProfileResponse updateProfile(UUID userId, BotProfilePatchRequest patch) {
        if (patch == null || !patch.hasAnyField()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no fields to update");
        }
        User user = loadActiveUser(userId);
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

        boolean needContacts = (patch.registration() != null && patch.registration().hasAny())
                || (patch.habitation() != null && patch.habitation().hasAny());
        if (needContacts) {
            Contacts contacts = loadContactsForUpdate(user.getId());
            boolean needSaveContactsRow = false;
            Address reg = contacts.getRegistration();
            Address hab = contacts.getHabitation();
            boolean regPatch = patch.registration() != null && patch.registration().hasAny();
            boolean habPatch = patch.habitation() != null && patch.habitation().hasAny();

            if (regPatch) {
                if (reg == null) {
                    reg = unconstrainedDataManager.create(Address.class);
                    contacts.setRegistration(reg);
                    needSaveContactsRow = true;
                }
                applyAddressPatch(reg, patch.registration());
            }
            if (habPatch) {
                if (hab == null) {
                    hab = unconstrainedDataManager.create(Address.class);
                    contacts.setHabitation(hab);
                    needSaveContactsRow = true;
                }
                applyAddressPatch(hab, patch.habitation());
            }

            if (reg != null && hab != null && reg.getId() != null && reg.getId().equals(hab.getId())) {
                unconstrainedDataManager.save(reg);
            } else {
                if (regPatch && reg != null) {
                    unconstrainedDataManager.save(reg);
                }
                if (habPatch && hab != null && (reg == null || reg.getId() == null || !reg.getId().equals(hab.getId()))) {
                    unconstrainedDataManager.save(hab);
                }
            }
            if (needSaveContactsRow) {
                unconstrainedDataManager.save(contacts);
            }
        }

        User refreshed = unconstrainedDataManager.load(User.class)
                .id(user.getId())
                .fetchPlan(this::buildUserProfileFetchPlan)
                .one();
        return toResponse(refreshed);
    }

    private void buildUserProfileFetchPlan(FetchPlanBuilder fp) {
        fp.add("firstName")
                .add("lastName")
                .add("patronymic")
                .add("contactsInfo", c -> c
                        .add("phoneNumber")
                        .add("registration", this::buildAddressFetchPlan)
                        .add("habitation", this::buildAddressFetchPlan))
                .add("serviceInfo", sf -> sf
                        .add("status")
                        .add("department", d -> d.add("number"))
                        .add("idCard", ic -> ic.add("issued").add("until").add("spl"))
                        .add("breastplate")
                        .add("medicalExamination")
                        .add("rank")
                        .add("post"));
    }

    private void buildAddressFetchPlan(FetchPlanBuilder fp) {
        fp.add("index")
                .add("city")
                .add("street")
                .add("houseNumber")
                .add("body")
                .add("flat")
                .add("typeOfHousing")
                .add("statusOfHousing");
    }

    private User loadActiveUser(UUID userId) {
        activeUserChecker.requireActive(userId);
        return unconstrainedDataManager.load(User.class)
                .id(userId)
                .fetchPlan(this::buildUserProfileFetchPlan)
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
        Contacts contacts = user.getContactsInfo();
        String phone = contacts != null ? contacts.getPhoneNumber() : null;
        Address reg = contacts != null ? contacts.getRegistration() : null;
        Address hab = contacts != null ? contacts.getHabitation() : null;
        return new BotProfileResponse(
                user.getId(),
                user.getDisplayName(),
                rankMessage(rank),
                postMessage(post),
                departmentLabel(dept),
                si.getBreastplate(),
                medical,
                maskRussianMobile(phone),
                toAddressResponse(reg),
                toAddressResponse(hab),
                card != null ? card.getIssued() : null,
                card != null ? card.getUntil() : null
        );
    }

    private BotAddressResponse toAddressResponse(Address a) {
        if (a == null) {
            return null;
        }
        TypeOfHousing t = a.getTypeOfHousing();
        StatusOfHousing s = a.getStatusOfHousing();
        String summary = summarizeAddress(a);
        return new BotAddressResponse(
                a.getIndex(),
                a.getCity(),
                a.getStreet(),
                a.getHouseNumber(),
                a.getBody(),
                a.getFlat(),
                t == null ? null : t.getId(),
                s == null ? null : s.getId(),
                summary
        );
    }

    private static String summarizeAddress(Address a) {
        if (a == null) {
            return null;
        }
        String line = a.getInstanceName();
        if (line == null) {
            return null;
        }
        line = line.replaceAll(", +", ", ").replaceAll("^, |, $", "").trim();
        return line.isEmpty() ? null : line;
    }

    private Contacts loadContactsForUpdate(UUID userId) {
        return unconstrainedDataManager.load(Contacts.class)
                .query("select c from Contacts c where c.user.id = :uid")
                .parameter("uid", userId)
                .fetchPlan(cfp -> cfp
                        .add("user")
                        .add("registration", this::buildAddressFetchPlan)
                        .add("habitation", this::buildAddressFetchPlan))
                .optional()
                .orElseGet(() -> {
                    User u = unconstrainedDataManager.load(User.class).id(userId).one();
                    Contacts created = unconstrainedDataManager.create(Contacts.class);
                    created.setUser(u);
                    return created;
                });
    }

    private void applyAddressPatch(Address addr, BotAddressPatch p) {
        if (p.index() != null) {
            String ix = p.index().trim();
            if (!ix.isEmpty() && !INDEX_PATTERN.matcher(ix).matches()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "address index must be 6 digits");
            }
            addr.setIndex(ix.isEmpty() ? null : ix);
        }
        if (p.city() != null) {
            String v = trimToNull(p.city(), 50, "city");
            addr.setCity(v);
        }
        if (p.street() != null) {
            String v = trimToNull(p.street(), 255, "street");
            addr.setStreet(v);
        }
        if (p.houseNumber() != null) {
            String v = trimToNull(p.houseNumber(), 5, "houseNumber");
            addr.setHouseNumber(v);
        }
        if (p.body() != null) {
            String v = trimToNull(p.body(), 4, "body");
            addr.setBody(v);
        }
        if (p.flat() != null) {
            String v = trimToNull(p.flat(), 10, "flat");
            addr.setFlat(v);
        }
        if (p.typeOfHousing() != null) {
            String id = p.typeOfHousing().trim();
            if (id.isEmpty()) {
                addr.setTypeOfHousing((TypeOfHousing) null);
            } else {
                TypeOfHousing t = TypeOfHousing.fromId(id);
                if (t == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid typeOfHousing");
                }
                addr.setTypeOfHousing(t);
            }
        }
        if (p.statusOfHousing() != null) {
            String id = p.statusOfHousing().trim();
            if (id.isEmpty()) {
                addr.setStatusOfHousing((StatusOfHousing) null);
            } else {
                StatusOfHousing s = StatusOfHousing.fromId(id);
                if (s == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid statusOfHousing");
                }
                addr.setStatusOfHousing(s);
            }
        }
    }

    private static String trimToNull(String raw, int maxLen, String field) {
        if (raw == null) {
            return null;
        }
        String t = raw.trim();
        if (t.isEmpty()) {
            return null;
        }
        if (t.length() > maxLen) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " too long");
        }
        return t;
    }

    private static String maskRussianMobile(String e164) {
        if (e164 == null || !e164.startsWith("+7") || e164.length() != 12) {
            return null;
        }
        String last4 = e164.substring(8);
        return "+7 *** *** " + last4.substring(0, 2) + " " + last4.substring(2);
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
