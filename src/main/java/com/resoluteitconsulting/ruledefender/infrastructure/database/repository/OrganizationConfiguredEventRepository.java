package com.resoluteitconsulting.ruledefender.infrastructure.database.repository;

import com.resoluteitconsulting.ruledefender.infrastructure.database.model.OrganizationConfiguredEvent;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.OrganizationConfiguredEventId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationConfiguredEventRepository extends JpaRepository<OrganizationConfiguredEvent, OrganizationConfiguredEventId> {

    List<OrganizationConfiguredEvent> findByOrganizationId(Long organizationId);
    Optional<OrganizationConfiguredEvent> findByEventConfigurationIdAndOrganizationId(Long eventConfigurationId, Long organizationId);

}