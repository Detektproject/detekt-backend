package com.resoluteitconsulting.ruledefender.domain.usecases;

import com.resoluteitconsulting.ruledefender.infrastructure.database.model.Organization;

public interface SubscriptionService {

    void activateSubscription(Long organizationId, Long planId);

    void activateDefaultSubscription(Organization organization);

    void cancelSubscription(Long subscriptionId);

    void renewSubscription(Long subscriptionId);

    void checkAndUpdateSubscriptionStatuses();

    void renewSubscriptions();

}
