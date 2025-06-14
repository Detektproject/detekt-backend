package com.resoluteitconsulting.ruledefender.infrastructure.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "detected_operation_rule")
@Getter
@Setter
public class DetectedOperationRule extends EntityBase{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "detected_operation_rule_seq_gen")
    @SequenceGenerator(name = "detected_operation_rule_seq_gen", sequenceName = "detected_operation_rule_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "operation_id")
    @JsonIgnore
    private Operation operation;

    @ManyToOne
    @JoinColumn(name = "rule_id")
    private RuleEntity rule;

    @Column(name = "operation_id", insertable = false, updatable = false)
    private Long operationId;

    @Column(name = "rule_id", insertable = false, updatable = false)
    private Long ruleId;

}
