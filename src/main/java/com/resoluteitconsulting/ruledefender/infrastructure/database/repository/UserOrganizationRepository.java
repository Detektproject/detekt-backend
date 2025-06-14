package com.resoluteitconsulting.ruledefender.infrastructure.database.repository;

import com.resoluteitconsulting.ruledefender.infrastructure.database.model.UserOrganization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserOrganizationRepository extends JpaRepository<UserOrganization, Long> {

    Optional<UserOrganization> findByUserIdAndActivated(String userId, boolean isActive);

    Page<UserOrganization> findByOrganizationId(Long organizationId, Pageable pageable);

    Optional<UserOrganization> findByEmail(String email);

}