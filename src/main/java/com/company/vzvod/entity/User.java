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
import io.jmix.security.authentication.JmixUserDetails;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

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
    @Column(name = "FIRST_NAME", nullable = false, length = 20)
    private String firstName;

    @NotEmpty(message = "{msg://com.company.vzvod.entity/User.lastName.validation.NotEmpty}")
    @NotBlank(message = "{msg://com.company.vzvod.entity/User.lastName.validation.NotBlank}")
    @Column(name = "LAST_NAME", nullable = false, length = 20)
    private String lastName;

    @NotEmpty(message = "{msg://com.company.vzvod.entity/User.patronymic.validation.NotEmpty}")
    @NotBlank(message = "{msg://com.company.vzvod.entity/User.patronymic.validation.NotBlank}")
    @Column(name = "PATRONYMIC", nullable = false, length = 20)
    private String patronymic;

    @Past(message = "{msg://com.company.vzvod.entity/User.dateOfBirth.validation.Past}")
    @Column(name = "DATE_OF_BIRTH", nullable = false)
    private LocalDate dateOfBirth;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "user", optional = false, cascade = CascadeType.ALL)
    private ServiceInfo serviceInfo;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "user")
    private Contacts contactsInfo;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "EDUCATION_ID")
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Education education;

    @Column(name = "ARMY_SERVICE", nullable = false)
    private Boolean armyService = false;

    @OneToMany(mappedBy = "user")
    private List<Vehicle> vehicleInfo;

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

}
