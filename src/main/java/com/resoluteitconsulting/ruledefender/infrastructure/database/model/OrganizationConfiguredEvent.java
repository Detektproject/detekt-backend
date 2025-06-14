package com.resoluteitconsulting.ruledefender.infrastructure.database.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Table(name = "organization_configured_event")
@Entity
@IdClass(OrganizationConfiguredEventId.class)
@Getter
@Setter
public class OrganizationConfiguredEvent extends EntityBase {

    @Id
    @Column(name = "organization_id")
    private Long organizationId;

    @Id
    @Column(name = "event_configuration_id")
    private Long eventConfigurationId;

}
