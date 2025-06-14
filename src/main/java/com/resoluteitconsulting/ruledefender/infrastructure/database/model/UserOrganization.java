package com.resoluteitconsulting.ruledefender.infrastructure.database.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.resoluteitconsulting.ruledefender.infrastructure.database.converters.BooleanConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_organization")
@SQLDelete(sql = "UPDATE user_organization SET deleted = 'Y' WHERE id=?")
@Where(clause = "deleted='N'")
@Getter
@Setter
public class UserOrganization extends EntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_organization_gen")
    @SequenceGenerator(name = "user_organization_gen", sequenceName = "user_organization_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "email", length = 300)
    private String email;

    @JsonBackReference
    @JoinColumn(name = "organization_id")
    @ManyToOne(fetch = FetchType.EAGER)
    private Organization organization;

    @Column(name = "organization_id", insertable = false, updatable = false)
    private Long organizationId;

    @Column(name = "user_id")
    private String userId;

    @Convert(converter= BooleanConverter.class)
    @Column(name = "status", length = 1)
    private Boolean status;

    @Convert(converter=BooleanConverter.class)
    @Column(name = "activated", length = 1)
    private Boolean activated;

    @Column(name = "activation_code", length = 1)
    private String activationCode;

    @Column(name = "activation_date")
    private OffsetDateTime activationDate;

    @Column(name = "activation_send_date")
    private OffsetDateTime activationSendDate;

}
