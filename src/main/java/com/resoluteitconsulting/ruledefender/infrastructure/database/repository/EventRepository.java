package com.resoluteitconsulting.ruledefender.infrastructure.database.repository;

import com.resoluteitconsulting.ruledefender.infrastructure.database.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {
    List<Event> findTop5ByOrganizationIdOrderByCreatedOnDesc(Long organisationId);
}