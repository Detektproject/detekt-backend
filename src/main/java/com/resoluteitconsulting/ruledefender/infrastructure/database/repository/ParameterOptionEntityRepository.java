package com.resoluteitconsulting.ruledefender.infrastructure.database.repository;

import com.resoluteitconsulting.ruledefender.infrastructure.database.model.ParameterOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParameterOptionEntityRepository extends JpaRepository<ParameterOptionEntity, Long> {
}