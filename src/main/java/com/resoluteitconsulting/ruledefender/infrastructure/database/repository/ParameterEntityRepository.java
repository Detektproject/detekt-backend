package com.resoluteitconsulting.ruledefender.infrastructure.database.repository;

import com.resoluteitconsulting.ruledefender.infrastructure.database.model.ParameterEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParameterEntityRepository extends JpaRepository<ParameterEntity, Long> {

    Page<ParameterEntity> findByOrganizationId(Long organizationId, Pageable pageable);

}