package com.resoluteitconsulting.ruledefender.infrastructure.database.model;

import java.time.LocalDate;

public interface OrganizationSummaryDetail {

    Long getCount();

    LocalDate getOperationDate();

    String getIsAnomaly();

}