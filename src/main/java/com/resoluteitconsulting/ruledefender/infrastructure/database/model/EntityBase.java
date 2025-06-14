package com.resoluteitconsulting.ruledefender.infrastructure.database.model;

import com.resoluteitconsulting.ruledefender.infrastructure.database.converters.BooleanConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@MappedSuperclass
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class EntityBase {

    @CreatedDate
    @Column(name = "created_on")
    private Instant createdOn;

    @CreatedBy
    @Column(name = "created_by")
    private String createdBydd;

    @LastModifiedDate
    @Column(name = "updated_on")
    private Instant updatedOn;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBydd;

    @Column(name = "deleted", length = 1)
    @Convert(converter = BooleanConverter.class)
    private Boolean deleted;
}
