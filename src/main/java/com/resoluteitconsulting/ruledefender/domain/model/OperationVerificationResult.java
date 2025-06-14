package com.resoluteitconsulting.ruledefender.domain.model;

import lombok.*;

@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OperationVerificationResult {

    private Long operationId;
    private double score;

}
