package com.resoluteitconsulting.ruledefender.infrastructure.database.service;

import com.resoluteitconsulting.ruledefender.domain.usecases.OrganizationBillingService;
import com.resoluteitconsulting.ruledefender.domain.usecases.SubscriptionLimitingService;
import com.resoluteitconsulting.ruledefender.domain.usecases.SubscriptionService;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.Organization;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.OrganizationSubscription;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.SubscriptionPlan;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.OrganizationRepository;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.OrganizationSubscriptionRepository;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.SubscriptionPlanRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationSubscriptionRepository organizationSubscriptionRepository;
    private final OrganizationBillingService billingService;
    private final SubscriptionLimitingService subscriptionLimitingService;


    @Override
    @Transactional
    public void activateSubscription(Long organizationId, Long planId) {
        Optional<SubscriptionPlan> subscriptionPlan = subscriptionPlanRepository.findById(planId);

        if (subscriptionPlan.isEmpty()) {
            throw new IllegalArgumentException("Invalid subscription plan ID");
        }

        Optional<Organization> organization = organizationRepository.findById(organizationId);

        if (organization.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No organization found with the provided id"
            );

        Optional<OrganizationSubscription> currentSubscription = organizationSubscriptionRepository.findByOrganizationIdAndStatus(organizationId, "Active");

        if (currentSubscription.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No subscription plan has been found with the provided organization id"
            );

        currentSubscription.get().setEndDate(LocalDate.now());
        currentSubscription.get().setStatus("Suspended");

        organizationSubscriptionRepository.save(currentSubscription.get());

        Optional<SubscriptionPlan> newSubscriptionPlan = subscriptionPlanRepository.findById(subscriptionPlan.get().getId());
        newSubscriptionPlan.ifPresent(
                subscription -> {
                    OrganizationSubscription organizationSubscription = new OrganizationSubscription();
                    if (Boolean.TRUE.equals(subscription.getIsDefault())) {
                        organizationSubscription.setEndDate(LocalDate.now().plusYears(1));
                        organizationSubscription.setStartDate(LocalDate.now().plusDays(1));
                    } else {
                        organizationSubscription.setEndDate(LocalDate.now().plusMonths(1));
                        organizationSubscription.setStartDate(LocalDate.now());

                    }
                    organizationSubscription.setOrganization(organization.get());
                    organizationSubscription.setPlan(subscription);
                    organizationSubscription.setStatus("Active");
                    organizationSubscriptionRepository.save(organizationSubscription);

                }
        );


    }

    @Override
    public void activateDefaultSubscription(Organization organization) {

        Optional<SubscriptionPlan> freeSubscriptionPlan = subscriptionPlanRepository.findByIsDefault(Boolean.TRUE);
        freeSubscriptionPlan.ifPresent(
                subscriptionPlan -> {
                    OrganizationSubscription organizationSubscription = new OrganizationSubscription();
                    organizationSubscription.setOrganization(organization);
                    organizationSubscription.setPlan(subscriptionPlan);
                    organizationSubscription.setStatus("Active");
                    organizationSubscription.setStartDate(LocalDate.now());
                    organizationSubscription.setEndDate(LocalDate.MAX);
                    organizationSubscriptionRepository.save(organizationSubscription);
                }
        );

    }

    @Override
    @Transactional
    public void cancelSubscription(Long subscriptionId) {
        Optional<OrganizationSubscription> organizationSubscription = organizationSubscriptionRepository.findById(subscriptionId);

        if (organizationSubscription.isEmpty()) {
            throw new IllegalArgumentException("Invalid subscription ID");
        }

        if (Boolean.TRUE.equals(organizationSubscription.get().getPlan().getIsDefault())) {
            throw new IllegalArgumentException("Default subscription cannot be canceled");
        }

        organizationSubscription.get().setStatus("Canceled");
        organizationSubscription.get().setEndDate(LocalDate.now());

        organizationSubscriptionRepository.save(organizationSubscription.get());

        activateDefaultSubscription(organizationSubscription.get().getOrganization());

    }

    @Override
    public void renewSubscription(Long subscriptionId) {
        Optional<OrganizationSubscription> organizationSubscription = organizationSubscriptionRepository.findById(subscriptionId);

        if (organizationSubscription.isEmpty()) {
            throw new IllegalArgumentException("Invalid subscription ID");
        }
        organizationSubscription.get().setEndDate(LocalDate.now().plusMonths(1));
        organizationSubscriptionRepository.save(organizationSubscription.get());
    }

    @Override
    @Transactional
    public void checkAndUpdateSubscriptionStatuses() {
        log.info("Start checking for subscription status");

        Pageable pageable = PageRequest.of(0, 100); // Adjust page size as needed
        Slice<OrganizationSubscription> slice;
        do {
            slice = organizationSubscriptionRepository.findByStatusAndEndDateBefore("Active", LocalDate.now(), pageable);
            slice.getContent().forEach(subscription -> {
                log.info("Subscription {} has been expired, status will be updated and default subscription will be activated", subscription.getId());
                subscription.setStatus("Expired");
                organizationSubscriptionRepository.save(subscription);
                activateDefaultSubscription(subscription.getOrganization());
            });
            // Move to the next slice
            pageable = slice.nextPageable();
        } while (slice.hasNext());

    }

    @Override
    @Transactional
    public void renewSubscriptions() {
        Pageable pageable = PageRequest.of(0, 100); // Adjust page size as needed
        Slice<OrganizationSubscription> slice;
        do {
            slice = organizationSubscriptionRepository.findByStatusAndEndDateBefore("Active", LocalDate.now(), pageable);
            slice.getContent().forEach(subscription -> {
                log.info("Renewing Subscription {}", subscription.getId());
                billingService.chargeSubscription(subscription);
                renewSubscription(subscription.getId());
            });
            pageable = slice.nextPageable();
        } while (slice.hasNext());
    }
}
