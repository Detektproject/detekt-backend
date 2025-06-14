package com.resoluteitconsulting.ruledefender.infrastructure.database.model;


import com.resoluteitconsulting.ruledefender.infrastructure.database.converters.BooleanConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "machine_learning_attributes")
public class MachineLearningAttributes extends EntityBase{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "machine_learning_attributes_gen")
    @SequenceGenerator(name = "machine_learning_attributes_gen", sequenceName = "machine_learning_attributes_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "operation_schema_id")
    private Long operationSchemaId;

    @Column(name = "attribute_name")
    private String attributeName;

    @Column(name = "attribute_value_path")
    private String attributeValuePath;

    @Column(name = "attribute_type")
    private String attributeType;

    @Column(name = "is_active")
    @Convert(converter= BooleanConverter.class)
    private Boolean isActive;

}
