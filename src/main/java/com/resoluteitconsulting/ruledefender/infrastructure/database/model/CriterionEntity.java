package com.resoluteitconsulting.ruledefender.infrastructure.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.resoluteitconsulting.ruledefender.domain.model.AggregationType;
import com.resoluteitconsulting.ruledefender.domain.model.Condition;
import com.resoluteitconsulting.ruledefender.domain.model.TimeWindowType;
import com.resoluteitconsulting.ruledefender.infrastructure.database.converters.BooleanConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "criterion")
@Getter
@Setter
public class CriterionEntity extends EntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "criterion", cascade = CascadeType.ALL)
    private Set<ConditionEntity> conditions;

    private String aggregationName;

    @Enumerated(EnumType.STRING)
    private AggregationType aggregationType;

    private String aggregationPath;

    private String aggregationValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    @JsonIgnore
    private RuleEntity rule;

    @Column(name = "rule_id", updatable = false, insertable = false)
    private Long ruleId;

    @Column(name = "is_active", length = 1)
    @Convert(converter = BooleanConverter.class)
    private Boolean isActive;

}
