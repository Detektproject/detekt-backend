package com.resoluteitconsulting.ruledefender.domain.model;

public record FraudDetectionResult(
        double ruleFraudScore,
        String aiFraudPrediction,
        String aiFraudPredictionScore
) {
}
