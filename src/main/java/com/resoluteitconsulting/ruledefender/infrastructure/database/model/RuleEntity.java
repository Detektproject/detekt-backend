package com.resoluteitconsulting.ruledefender.infrastructure.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.resoluteitconsulting.ruledefender.domain.model.TimeWindowType;
import com.resoluteitconsulting.ruledefender.infrastructure.database.converters.BooleanConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;

import java.time.LocalDate;
import java.util.Set;

@Entity(name = "rule")
@Table(name = "rule")
@Getter
@Setter
public class RuleEntity extends EntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rule_gen")
    @SequenceGenerator(name = "rule_gen", sequenceName = "rule_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    private String name;

    private String description;

    @Column(name = "is_active", length = 1)
    @Convert(converter = BooleanConverter.class)
    private Boolean isActive;

    @Column(name = "interval_type")
    @Enumerated(value = EnumType.STRING)
    private TimeWindowType intervalType;

    @Column(name = "interval_value")
    private Long intervalValue;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "rule")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL)
    private Set<CriterionEntity> criteria;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "rule")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL)
    private Set<Action> actions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_schema_id")
    @JsonIgnore
    private OperationSchema operationSchema;

    @Column(name = "operation_schema_id", insertable = false, updatable = false)
    private Long operationSchemaId;

    @Column(name = "severity")
    private String weight;

    @OneToMany(mappedBy = "rule", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<DetectedOperationRule> detectedOperationRules;

}
