package com.resoluteitconsulting.ruledefender.infrastructure.database.model;

import com.resoluteitconsulting.ruledefender.infrastructure.database.converters.BooleanConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "machine_learning_metadata")
public class MachineLearningMetadata extends EntityBase{

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "operation_schema_id")
    private Long operationSchemaId;

    @Column(name = "last_build_time")
    private OffsetDateTime lastBuildTime;

    @Column(name = "accuracy")
    private double accuracy;

    @Column(name = "data_size")
    private long dataSize;

    @Column(name = "is_active")
    @Convert(converter = BooleanConverter.class)
    private boolean isActive;

    @Column(name = "status")
    private String status;
}