package com.resoluteitconsulting.ruledefender.infrastructure.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "organization_parameter_option")
@Getter
@Setter
public class ParameterOptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String value;

    @Column(name = "parameter_id", nullable = false, insertable = false, updatable = false)
    private Long parameterId;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "parameter_id")
    private ParameterEntity parameter;


}
