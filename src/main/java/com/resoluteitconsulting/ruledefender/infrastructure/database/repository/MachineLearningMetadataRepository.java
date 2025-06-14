package com.resoluteitconsulting.ruledefender.infrastructure.database.repository;

import com.resoluteitconsulting.ruledefender.infrastructure.database.model.MachineLearningMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MachineLearningMetadataRepository extends JpaRepository<MachineLearningMetadata, String> {

    boolean existsByOperationSchemaIdAndStatus(Long operationSchemaId, String status);

    Page<MachineLearningMetadata> findByOperationSchemaId(Long operationSchemaId, Pageable pageable);

    Optional<MachineLearningMetadata> findFirstByStatusAndIsActiveAndOperationSchemaIdOrderByLastBuildTimeDesc(String status, boolean isActive, Long operationSchemaId);

}