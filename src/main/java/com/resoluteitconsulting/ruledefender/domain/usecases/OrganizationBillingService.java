package com.resoluteitconsulting.ruledefender.domain.usecases;

import com.resoluteitconsulting.ruledefender.infrastructure.database.model.OrganizationSubscription;

public interface OrganizationBillingService {

    void chargeSubscription(OrganizationSubscription organizationSubscription);

    void generateInvoice(OrganizationSubscription subscription);
}
