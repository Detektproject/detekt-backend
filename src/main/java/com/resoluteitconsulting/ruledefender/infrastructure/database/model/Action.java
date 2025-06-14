package com.resoluteitconsulting.ruledefender.infrastructure.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.resoluteitconsulting.ruledefender.infrastructure.database.converters.BooleanConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "action")
@Getter
@Setter
public class Action extends EntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "action_gen")
    @SequenceGenerator(name = "action_gen", sequenceName = "action_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    private String name;
    private String value;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    private RuleEntity rule;
    @Column(name = "rule_id", updatable = false, insertable = false)
    private Long ruleId;

    @Column(name = "is_active", length = 1)
    @Convert(converter = BooleanConverter.class)
    private Boolean isActive;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endpoint_id")
    private Endpoint endpoint;
    @Column(name = "endpoint_id", updatable = false, insertable = false)
    private Long endpointId;

}
