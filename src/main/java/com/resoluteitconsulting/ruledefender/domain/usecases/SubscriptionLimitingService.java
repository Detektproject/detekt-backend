package com.resoluteitconsulting.ruledefender.domain.usecases;

public interface SubscriptionLimitingService {

    void resetOperationLimits(String apiKey);

    boolean isProcessOperationEligible(String apiKey);

    boolean isAddEndpointEligible(String apiKey);

    boolean isAddSchemaEligible(String apiKey);

    boolean isAddRuleEligible(String apiKey, Long schemaId);

    boolean isAddActionEligible(Long ruleId);

}
