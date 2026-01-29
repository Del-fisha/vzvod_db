package com.company.vzvod.entity;

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@JmixEntity
@Table(name = "ADMINISTRATIVE_VIOLATION")
@Entity
public class AdministrativeViolation extends Violation {

    @InstanceName
    @Column(name = "ARTICLE_OF_ADMINISTRATIVE")
    private Integer article;

    public Integer getType() {
        return article;
    }

    public void setType(ArticleOfAdministrative article) {
        this.article = article == null ? null : article.getId();;
    }
}