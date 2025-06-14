package com.resoluteitconsulting.ruledefender.infrastructure.database.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Table(name = "event")
@Entity(name = "event")
@Getter
@Setter
public class Event extends EntityBase {



    private String severity;
    private String type;
    private String content;
    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "organization_id")
    private Long organizationId;

}
