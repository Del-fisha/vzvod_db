package com.company.vzvod.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.util.UUID;

@JmixEntity
@Table(name = "SERVICE_INFO", indexes = {
        @Index(name = "IDX_SERVICE_INFO_USER", columnList = "USER_ID"),
        @Index(name = "IDX_SERVICE_INFO_ID_CARD", columnList = "ID_CARD_ID"),
        @Index(name = "IDX_SERVICE_INFO_DEPARTMENT", columnList = "DEPARTMENT_ID")
})
@Entity
public class ServiceInfo {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "USER_ID", nullable = false)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @Column(name = "DEPARTMENT")
    private Integer department;

    @Column(name = "RANK_")
    private String rank;

    @Column(name = "STATUS", nullable = false)
    private Integer status;

    @Column(name = "POST")
    private String post;

    @InstanceName
    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "ID_CARD_ID")
    @OneToOne(fetch = FetchType.LAZY)
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

    public Dep getDepartment() {
        return department == null ? null : Dep.fromId(department);
    }

    public void setDepartment(Dep department) {
        this.department = department == null ? null : department.getId();
    }

    public Rank getRank() {
        return rank == null ? null : Rank.fromId(rank);
    }

    public void setRank(Rank rank) {
        this.rank = rank == null ? null : rank.getId();
    }

    public IdCard getIdCard() {
        return idCard;
    }

    public void setIdCard(IdCard idCard) {
        this.idCard = idCard;
    }

    public LocalDate getStartOfPost() {
        return startOfPost;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public String getBreastplate() {
        return breastplate;
    }

    public void setBreastplate(String breastplate) {
        this.breastplate = breastplate;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

}