package com.resoluteitconsulting.ruledefender.infrastructure.database.repository;

import com.resoluteitconsulting.ruledefender.infrastructure.database.model.Operation;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.OrganizationSummaryDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationRepository extends JpaRepository<Operation, Long>, QuerydslPredicateExecutor<Operation> {

    List<Operation> findByRequestKeyAndOperationSchemaId(String requestKey, Long operationSchemaId);

    Page<Operation> findByOrganizationIdOrderByCreatedOnDesc(Long organizationId, Pageable pageable);

    long countByOrganizationIdAndIsAnomaly(Long organizationId, boolean isAnomaly);
    long countByOrganizationId(Long organizationId);

    @Query(value="select count(*) as count, operation_date as operationDate, is_anomaly as isAnomaly from operation where organization_id=?1 group by operation_date, is_anomaly",nativeQuery = true)
    List<OrganizationSummaryDetail> getOrganizationSummaryDetails(Long organizationId);

    Slice<Operation> findAllByOperationSchemaId(long schemaId, Pageable pageable);
}