package com.resoluteitconsulting.ruledefender.infrastructure.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.resoluteitconsulting.ruledefender.domain.model.Operator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "criterion_condition")
@Getter
@Setter
public class ConditionEntity extends EntityBase{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String fieldName;

    private String fieldPath;

    @Enumerated(value = EnumType.STRING)
    private Operator operator;

    private String value;

    @ManyToOne
    @JoinColumn(name = "criterion_id")
    @JsonIgnore
    private CriterionEntity criterion;
}
