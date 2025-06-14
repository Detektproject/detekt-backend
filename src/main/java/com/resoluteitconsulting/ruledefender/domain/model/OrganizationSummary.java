package com.resoluteitconsulting.ruledefender.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrganizationSummary {
    private Long totalAnomalies;
    private Long totalNovels;
    private Long currentTotalOperations;
    private Long previousTotalOperations;
    private Long currentTotalAnomalyOperations;
    private Long previousTotalAnomalyOperations;
    private Long currentTotalNovelOperations;
    private Long previousTotalNovelOperations;
}
