package com.company.vzvod.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
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

//    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "USER_ID", nullable = false)
    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
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



    public Qualification getQualificationClass() {
        return qualificationClass;
    }

    public void setQualificationClass(Qualification qualificationClass) {
        this.qualificationClass = qualificationClass;
    }

    public Boolean getMedicalExamination() {
        return medicalExamination;
    }

    public void setMedicalExamination(Boolean medicalExamination) {
        this.medicalExamination = medicalExamination;
    }

    public List<Vocation> getVocations() {
        return vocations;
    }

    public void setVocations(List<Vocation> vocations) {
        this.vocations = vocations;
    }

    public List<Incentive> getIncentive() {
        return incentive;
    }

    public void setIncentive(List<Incentive> incentive) {
        this.incentive = incentive;
    }

    public List<Penalty> getPenalty() {
        return penalty;
    }

    public void setPenalty(List<Penalty> penalty) {
        this.penalty = penalty;
    }

    public void setStartOfPost(LocalDate startOfPost) {
        this.startOfPost = startOfPost;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public Department getDepartment() {
        return this.department;
    }

    public void setDepartment(Department department) {
        this.department = department;
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

    public Set<Shift> getShifts() {
        return shifts;
    }

    public void setShifts(Set<Shift> shifts) {
        this.shifts = shifts;
    }
}