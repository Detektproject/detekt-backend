package com.resoluteitconsulting.ruledefender.infrastructure.database.repository;

import com.resoluteitconsulting.ruledefender.infrastructure.database.model.OperationSchema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OperationSchemaRepository extends JpaRepository<OperationSchema, Long> {

    Page<OperationSchema> findByOrganizationId(Long organizationId, Pageable pageRequest);
    Page<OperationSchema> findByOrganizationIdAndIsAIEnabled(Long organizationId, boolean isAIEnabled, Pageable pageRequest);

    Optional<OperationSchema> findBySchemaIdentifier(String schemaIdentifier);

}