package com.resoluteitconsulting.ruledefender.infrastructure.database.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.resoluteitconsulting.ruledefender.infrastructure.database.converters.BooleanConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Set;

@Entity(name = "operation")
@Table(name = "operation")
@Getter
@Setter
public class Operation extends EntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "operation_seq_gen")
    @SequenceGenerator(name = "operation_seq_gen", sequenceName = "operation_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "operation_schema_id")
    private OperationSchema operationSchema;

    @Column(name = "received_at")
    private Instant receivedAt;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    @JsonIgnore
    private Organization organization;

    @Column(name = "operation_schema_id", insertable = false, updatable = false)
    private Long operationSchemaId;

    @Column(name = "request_key")
    private String requestKey;

    @Column(name = "request_content")
    private String requestContent;

    @Column(name = "is_anomaly")
    @Convert(converter = BooleanConverter.class)
    private Boolean isAnomaly;

    @Column(name = "organization_id", insertable = false, updatable = false)
    private Long organizationId;

    @Column(name = "operation_date")
    private LocalDate operationDate;

    @OneToMany(mappedBy = "operation", cascade = CascadeType.ALL)
    private Set<DetectedOperationRule> detectedRules;

    @Column(name = "prediction_score")
    private String predictionScore;

    @Column(name = "prediction_result")
    private String predictionResult;

}
