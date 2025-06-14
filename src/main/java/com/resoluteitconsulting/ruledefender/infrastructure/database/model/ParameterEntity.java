package com.resoluteitconsulting.ruledefender.infrastructure.database.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "organization_parameter")
@Getter
@Setter
public class ParameterEntity extends EntityBase{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false)
    private Long organizationId;

    @OneToMany(mappedBy = "parameter", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<ParameterOptionEntity> options;


}
