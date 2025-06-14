package com.resoluteitconsulting.ruledefender.infrastructure.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.resoluteitconsulting.ruledefender.infrastructure.database.converters.BooleanConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.Set;


@Entity(name = "operation_schema")
@Table(name = "operation_schema")
@SQLDelete(sql = "UPDATE operation_schema SET deleted = 'Y' WHERE id=?")
@Where(clause = "deleted='N'")
@Getter
@Setter
public class OperationSchema extends EntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "operation_schema_seq_gen")
    @SequenceGenerator(name = "operation_schema_seq_gen", sequenceName = "operation_schema_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "schema_identifier")
    private String schemaIdentifier;
    private String name;
    private String value;
    @Column(name = "schema_key")
    private String schemaKey;
    private String path;
    private String type;

    @Column(name = "is_ai_enabled")
    @Convert(converter = BooleanConverter.class)
    private Boolean isAIEnabled;

    @Column(name = "is_date_included")
    @Convert(converter = BooleanConverter.class)
    @JsonProperty("isDateIncluded")
    private boolean isDateIncluded;

    @Column(name = "date_path")
    private String datePath;

    @Column(name = "date_format")
    private String dateFormat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id")
    @JsonIgnore
    private Organization organization;

    @Column(name="organisation_id", insertable=false, updatable=false)
    private Long organizationId;

    @OneToMany(mappedBy = "operationSchema", fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<RuleEntity> rules;

    @OneToMany(mappedBy = "operationSchema", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Operation> operations;

}
