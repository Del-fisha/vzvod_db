package com.company.vzvod.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.*;

@JmixEntity
@Table(name = "SERVICE_INFO", indexes = {
        @Index(name = "IDX_SERVICE_INFO_USER", columnList = "USER_ID"),
        @Index(name = "IDX_SERVICE_INFO_ID_CARD", columnList = "ID_CARD_ID"),
        @Index(name = "IDX_SERVICE_INFO_DEPARTMENT", columnList = "DEPARTMENT_ID")
})
@Entity
@Getter
@Setter
public class ServiceInfo {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    //    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "USER_ID", nullable = false)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPARTMENT_ID")
    private Department department;

    @Column(name = "RANK_")
    private String rank;

    @Column(name = "STATUS", nullable = false)
    private Integer status;

    @Column(name = "POST")
    private String post;

    @Composition
//    @OnDelete(DeletePolicy.CASCADE)
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "ID_CARD_ID")
    private IdCard idCard;

    @NotBlank(message = "{msg://com.company.vzvod.entity/ServiceInfo.token.validation.NotBlank}")
    @NotEmpty(message = "{msg://com.company.vzvod.entity/ServiceInfo.token.validation.NotEmpty}")
    @Column(name = "TOKEN")
    private String token;

    @Column(name = "BREASTPLATE", length = 8)
    private String breastplate;

    @PastOrPresent(message = "{msg://com.company.vzvod.entity/ServiceInfo.startDate.validation.PastOrPresent}")
    @Column(name = "START_DATE")
    private LocalDate startDate;

    @PastOrPresent(message = "{msg://com.company.vzvod.entity/ServiceInfo.startOfPost.validation.PastOrPresent}")
    @Column(name = "START_OF_POST")
    private LocalDate startOfPost;

    @OneToMany(mappedBy = "userServiceInfo", cascade = CascadeType.ALL)
    private List<Penalty> penalty;

    @OneToMany(mappedBy = "userServiceInfo", cascade = CascadeType.ALL)
    private List<Incentive> incentive;

    @ManyToMany(mappedBy = "units")
    private Set<Shift> shifts;

    @OneToMany(mappedBy = "userServiceInfo", cascade = CascadeType.ALL)
    private List<Vocation> vocations;

    @Column(name = "MEDICAL_EXAMINATION")
    private Boolean medicalExamination = false;

    @Column(name = "QUALIFICATION_CLASS")
    private Qualification qualificationClass;


    public Rank getRank() {
        return rank == null ? null : Rank.fromId(rank);
    }

    public void setRank(Rank rank) {
        this.rank = rank == null ? null : rank.getId();
    }

    public Post getPost() {
        return post == null ? null : Post.fromId(post);
    }

    public void setPost(Post post) {
        this.post = post == null ? null : post.getId();
    }

    public StatusInService getStatus() {
        return status == null ? null : StatusInService.fromId(status);
    }

    public void setStatus(StatusInService statusInService) {
        this.status = statusInService == null ? null : statusInService.getId();
    }

    @InstanceName
    @DependsOnProperties("user")
    public String getInstanceName() {
        if (user == null) {
            return "";
        }

        return String.format("%s %s. %s.",
                user.getLastName(),
                user.getFirstName().charAt(0),
                user.getPatronymic().charAt(0));
    }
}