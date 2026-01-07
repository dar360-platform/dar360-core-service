package com.lending.dar360UserService.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.OffsetDateTime;

@MappedSuperclass
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable<U> implements Serializable {

    private static final long serialVersionUID = 5282450495494154675L;

    @Column(name = "created_date", nullable = false, updatable = false)
    @CreatedDate
    protected OffsetDateTime createdDate;

    @Column(name = "modified_date")
    @LastModifiedDate
    protected OffsetDateTime modifiedDate;

    @Column(name = "created_by", nullable = false, updatable = false)
    @CreatedBy
    protected U createdBy;

    @Column(name = "modified_by", nullable = false)
    @LastModifiedBy
    protected U modifiedBy;

    public void updateAudit(U author) {
        if (null == author) {
            author = (U) (System
                    .getProperties()
                    .get("user.name")
                    .toString());
        }
        this.createdBy = author;
        this.modifiedBy = author;
        this.createdDate = OffsetDateTime.now();
        this.modifiedDate = OffsetDateTime.now();
    }
}

