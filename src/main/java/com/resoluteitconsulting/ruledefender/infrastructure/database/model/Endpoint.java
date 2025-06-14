package com.resoluteitconsulting.ruledefender.infrastructure.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.resoluteitconsulting.ruledefender.infrastructure.database.converters.BooleanConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Table(name = "endpoint")
@Entity(name = "endpoint")
@SQLDelete(sql = "UPDATE endpoint SET deleted = 'Y' WHERE id=?")
@Where(clause = "deleted='N'")
@Getter
@Setter
public class Endpoint extends EntityBase{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "endpoint_id_seq_gen")
    @SequenceGenerator(name = "endpoint_id_seq_gen", sequenceName = "endpoint_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    private String name;
    private String address;
    private String type;
    @Column(name = "is_active", length = 1)
    @Convert(converter = BooleanConverter.class)
    private Boolean isActive;
    private String certificate;
    private String description;
    private String direction;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(name = "organization_id", updatable = false, insertable = false)
    private Long organizationId;

}
