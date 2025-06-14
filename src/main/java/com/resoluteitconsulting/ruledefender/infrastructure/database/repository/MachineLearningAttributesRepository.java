package com.resoluteitconsulting.ruledefender.infrastructure.database.repository;

import com.resoluteitconsulting.ruledefender.infrastructure.database.model.MachineLearningAttributes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MachineLearningAttributesRepository extends JpaRepository<MachineLearningAttributes, Long> {

    List<MachineLearningAttributes> findByOperationSchemaId(Long operationSchemaId);

    Optional<MachineLearningAttributes> findByAttributeNameAndOperationSchemaId(String attributeName, Long operationSchemaId);

}