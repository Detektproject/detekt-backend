package com.resoluteitconsulting.ruledefender.domain.model;


public record Condition(
        String fieldName,
        String fieldPath,
        Operator operator,
        String value
) {
}
