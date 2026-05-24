package com.company.vzvod.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.annotation.Secret;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.security.authentication.JmixUserDetails;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import com.company.vzvod.security.crypto.EncryptedLocalDateConverter;
import com.company.vzvod.security.crypto.EncryptedStringConverter;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@JmixEntity
@Entity
@Table(name = "USER_", indexes = {
        @Index(name = "IDX_USER__ON_USERNAME", columnList = "USERNAME", unique = true),
        @Index(name = "IDX_USER__EDUCATION", columnList = "EDUCATION_ID")
})
public class User implements JmixUserDetails {

    @Id
    @Column(name = "ID", nullable = false)
    @JmixGeneratedValue
    private UUID id;

    @NotEmpty(message = "{msg://com.company.vzvod.entity/User.username.validation.NotEmpty}")
    @NotBlank(message = "{msg://com.company.vzvod.entity/User.username.validation.NotBlank}")
    @Column(name = "USERNAME", nullable = false)
    private String username;

    @Secret
    @SystemLevel
    @Column(name = "PASSWORD")
    private String password;

    @NotEmpty(message = "{msg://com.company.vzvod.entity/User.firstName.validation.NotEmpty}")
    @NotBlank(message = "{msg://com.company.vzvod.entity/User.firstName.validation.NotBlank}")
    @Size(max = 20)
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "FIRST_NAME", nullable = false, length = 512)
    private String firstName;

    @Column(name = "FIRST_NAME", insertable = false, updatable = false)
    private String firstNameRaw;

    @NotEmpty(message = "{msg://com.company.vzvod.entity/User.lastName.validation.NotEmpty}")
    @NotBlank(message = "{msg://com.company.vzvod.entity/User.lastName.validation.NotBlank}")
    @Size(max = 20)
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "LAST_NAME", nullable = false, length = 512)
    private String lastName;

    @Column(name = "LAST_NAME", insertable = false, updatable = false)
    private String lastNameRaw;

    @NotEmpty(message = "{msg://com.company.vzvod.entity/User.patronymic.validation.NotEmpty}")
    @NotBlank(message = "{msg://com.company.vzvod.entity/User.patronymic.validation.NotBlank}")
    @Size(max = 20)
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "PATRONYMIC", nullable = false, length = 512)
    private String patronymic;

    @Column(name = "PATRONYMIC", insertable = false, updatable = false)
    private String patronymicRaw;

    @Past(message = "{msg://com.company.vzvod.entity/User.dateOfBirth.validation.Past}")
    @Convert(converter = EncryptedLocalDateConverter.class)
    @Column(name = "DATE_OF_BIRTH_ENC")
    private LocalDate dateOfBirth;

    @Column(name = "DATE_OF_BIRTH_ENC", insertable = false, updatable = false)
    private String dateOfBirthEncRaw;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "user", optional = false, cascade = CascadeType.ALL)
    private ServiceInfo serviceInfo;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Contacts contactsInfo;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "EDUCATION_ID")
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Education education;

    @Column(name = "ARMY_SERVICE", nullable = false)
    private String armyService;

    @OneToMany(mappedBy = "user")
    private List<Vehicle> vehicleInfo;

    @Column(name = "GENDER")
    private String gender;

    @Transient
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities != null ? authorities : Collections.emptyList();
    }

    @Override
    public void setAuthorities(final Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    public Gender getGender() {
        return gender == null ? null : Gender.fromId(gender);
    }

    public void setGender(Gender gender) {
        this.gender = gender == null ? null : gender.getId();
    }

    public ArmyService getArmyService() {
        return armyService == null ? null : ArmyService.fromId(armyService);
    }

    public void setArmyService(ArmyService armyService) {
        this.armyService = armyService == null ? null : armyService.getId();
    }

    public String getFirstNameRaw() {
        return firstNameRaw;
    }

    public String getLastNameRaw() {
        return lastNameRaw;
    }

    public String getPatronymicRaw() {
        return patronymicRaw;
    }

    public String getDateOfBirthEncRaw() {
        return dateOfBirthEncRaw;
    }


    @InstanceName
    @DependsOnProperties({"firstName", "lastName"})
    public String getDisplayName() {
        String result = String.format("%s %s %s",
                        (lastName != null ? lastName : ""),
                        (firstName != null ? firstName : ""),
                        (patronymic != null ? patronymic : ""))
                .trim();

        return result.replaceAll("\\s+", " ");
    }

    @Transient
    @JmixProperty
    @DependsOnProperties({"firstName", "lastName", "patronymic"})
    public String getShortFio() {
        String ln = lastName == null ? "" : lastName.trim();
        String fn = firstName == null ? "" : firstName.trim();
        String pn = patronymic == null ? "" : patronymic.trim();

        StringBuilder sb = new StringBuilder();
        if (!ln.isBlank()) {
            sb.append(ln);
        }

        String fi = initial(fn);
        String pi = initial(pn);

        if (!fi.isBlank() || !pi.isBlank()) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            if (!fi.isBlank()) {
                sb.append(fi).append('.');
            }
            if (!pi.isBlank()) {
                sb.append(' ').append(pi).append('.');
            }
        }

        return sb.toString().trim();
    }

    private static String initial(String value) {
        if (value == null) {
            return "";
        }
        String v = value.trim();
        if (v.isBlank()) {
            return "";
        }
        int cp = v.codePointAt(0);
        return new String(Character.toChars(cp)).toUpperCase();
    }

}
