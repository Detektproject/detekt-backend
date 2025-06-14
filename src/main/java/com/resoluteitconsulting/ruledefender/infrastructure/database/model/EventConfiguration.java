package com.resoluteitconsulting.ruledefender.infrastructure.database.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Table(name = "event_configuration")
@Entity
@Getter
@Setter
public class EventConfiguration extends EntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_configuration_gen")
    @SequenceGenerator(name = "event_configuration_gen", sequenceName = "event_configuration_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    private String name;

    private String type;
    private String description;

}

