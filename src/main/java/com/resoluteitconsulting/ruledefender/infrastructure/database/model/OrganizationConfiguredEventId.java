package com.resoluteitconsulting.ruledefender.infrastructure.database.model;

import jakarta.persistence.Id;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

@Getter
public class OrganizationConfiguredEventId implements Serializable {

    @Id
    private Long organizationId;

    @Id
    private Long eventConfigurationId;

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public void setEventConfigurationId(Long eventConfigurationId) {
        this.eventConfigurationId = eventConfigurationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrganizationConfiguredEventId that = (OrganizationConfiguredEventId) o;
        return Objects.equals(organizationId, that.organizationId) && Objects.equals(eventConfigurationId, that.eventConfigurationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organizationId, eventConfigurationId);
    }
}
