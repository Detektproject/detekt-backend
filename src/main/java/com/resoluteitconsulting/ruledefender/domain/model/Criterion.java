package com.resoluteitconsulting.ruledefender.domain.model;

import java.util.List;

public record Criterion(
        Long id,
        String name,
        List<Condition> conditions,
        String aggregationName,
        AggregationType aggregationType,
        String aggregationPath,
        String aggregationValue,
        Long ruleId
) {
}
