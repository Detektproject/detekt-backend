package com.resoluteitconsulting.ruledefender.infrastructure.database.service;

import com.resoluteitconsulting.ruledefender.domain.usecases.OrganizationBillingService;
import com.resoluteitconsulting.ruledefender.domain.usecases.SubscriptionService;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.OrganizationSubscription;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class OrganizationBillingServiceImpl implements OrganizationBillingService {


    @Override
    public void chargeSubscription(OrganizationSubscription organizationSubscription) {
        log.info("Charging organization: {} for plan: {}", organizationSubscription.getOrganizationId(), organizationSubscription.getPlan().getName());

        // Optionally, generate an invoice for the subscription
        generateInvoice(organizationSubscription);
    }

    @Override
    public void generateInvoice(OrganizationSubscription subscription) {

    }
}
