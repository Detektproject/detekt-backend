package com.resoluteitconsulting.ruledefender.infrastructure.database.repository;

import com.resoluteitconsulting.ruledefender.infrastructure.database.model.OrganizationSubscription;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrganizationSubscriptionRepository extends JpaRepository<OrganizationSubscription, Long> {

    List<OrganizationSubscription> findByOrganizationId(Long organizationId);
    Optional<OrganizationSubscription> findByOrganizationIdAndStatus(Long organizationId, String status);
    Slice<OrganizationSubscription> findByStatusAndEndDateBefore(String status, LocalDate endDate, Pageable pageable);
}