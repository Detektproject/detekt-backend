package com.resoluteitconsulting.ruledefender.infrastructure.jobs;

import com.resoluteitconsulting.ruledefender.domain.usecases.OrganizationBillingService;
import com.resoluteitconsulting.ruledefender.domain.usecases.SubscriptionService;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.OrganizationSubscription;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.OrganizationSubscriptionRepository;
import com.stripe.service.BillingService;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.scheduling.cron.Cron;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDate;

@Configuration
@AllArgsConstructor
@Slf4j
public class SubscriptionSchedulerConfig {

    private final JobScheduler jobScheduler;
    private final SubscriptionService subscriptionService;

    @PostConstruct
    public void scheduleJobs() {
        // Schedule a daily job to check and update subscription statuses
        jobScheduler.scheduleRecurrently(
                "subscription-status-check", Cron.daily(),
                subscriptionService::checkAndUpdateSubscriptionStatuses
        );

        jobScheduler.scheduleRecurrently("subscription-renewal", Cron.daily(), subscriptionService::renewSubscriptions);
    }
}
