package com.company.vzvod.entity;

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@JmixEntity
@Table(name = "ADMINISTRATIVE_VIOLATION")
@Entity
public class AdministrativeViolation extends Violation {

    @InstanceName
    @Column(name = "ARTICLE")
    private ArticleOfAdministrative article;

    public void setArticle(ArticleOfAdministrative article) {
        this.article = article;
    }
}