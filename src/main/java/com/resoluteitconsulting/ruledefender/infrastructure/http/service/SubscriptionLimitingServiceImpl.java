package com.resoluteitconsulting.ruledefender.infrastructure.http.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.resoluteitconsulting.ruledefender.domain.usecases.SubscriptionLimitingService;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.OperationSchema;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.Organization;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.OrganizationSubscription;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.RuleEntity;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.OrganizationRepository;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.RuleRepository;
import io.github.bucket4j.*;
import io.github.bucket4j.grid.hazelcast.HazelcastProxyManager;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class SubscriptionLimitingServiceImpl implements SubscriptionLimitingService {

    private final HazelcastProxyManager<String> proxyManager;
    private final OrganizationRepository organizationRepository;
    private final RuleRepository ruleRepository;

    public SubscriptionLimitingServiceImpl(
            HazelcastInstance hzInstance,
            OrganizationRepository organizationRepository,
            RuleRepository ruleRepository
    ) {

        IMap<String, byte[]> map = hzInstance.getMap("bucket-map");
        this.proxyManager = new HazelcastProxyManager<>(map);
        this.organizationRepository = organizationRepository;
        this.ruleRepository = ruleRepository;
    }

    @Override
    public void resetOperationLimits(String apiKey) {

        Bucket bucket = resolveBucket(apiKey);

        Optional<Organization> organization = organizationRepository.findByApiKey(apiKey);
        if (organization.isEmpty())
            throw new IllegalArgumentException("API Key not recognized");

        Optional<OrganizationSubscription> activeSubscription = organization.get().getSubscriptions()
                .stream()
                .filter(OrganizationSubscription::isActive)
                .findFirst();

        if (activeSubscription.isEmpty())
            throw new IllegalStateException("No active subscription found");

        bucket.replaceConfiguration(
                newBucket(Integer.parseInt(activeSubscription.get().getPlan().getFeatures().get("requests_per_month"))),
               TokensInheritanceStrategy.RESET
        );
    }

    @Override
    public boolean isProcessOperationEligible(String apiKey) {
        Bucket bucket = resolveBucket(apiKey);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        return probe.isConsumed();
    }

    @Override
    public boolean isAddEndpointEligible(String apiKey) {
        Optional<Organization> organization = organizationRepository.findByApiKey(apiKey);
        if (organization.isEmpty())
            throw new IllegalArgumentException("API Key not recognized");

        Optional<OrganizationSubscription> activeSubscription = organization.get().getSubscriptions()
                .stream()
                .filter(OrganizationSubscription::isActive)
                .findFirst();

        if (activeSubscription.isEmpty())
            throw new IllegalStateException("No active subscription found");

        return Integer.parseInt(activeSubscription.get().getPlan().getFeatures().get("endpoints")) > organization.get().getEndpoints().size() + 1;
    }

    @Override
    public boolean isAddSchemaEligible(String apiKey) {
        Optional<Organization> organization = organizationRepository.findByApiKey(apiKey);
        if (organization.isEmpty())
            throw new IllegalArgumentException("API Key not recognized");

        Optional<OrganizationSubscription> activeSubscription = organization.get().getSubscriptions()
                .stream()
                .filter(OrganizationSubscription::isActive)
                .findFirst();

        if (activeSubscription.isEmpty())
            throw new IllegalStateException("No active subscription found");

        return Integer.parseInt(activeSubscription.get().getPlan().getFeatures().get("schemas")) > organization.get().getOperationSchemas().size() + 1;
    }

    @Override
    public boolean isAddRuleEligible(String apiKey, Long schemaId) {
        Optional<Organization> organization = organizationRepository.findByApiKey(apiKey);
        if (organization.isEmpty())
            throw new IllegalArgumentException("API Key not recognized");

        Optional<OrganizationSubscription> activeSubscription = organization.get().getSubscriptions()
                .stream()
                .filter(OrganizationSubscription::isActive)
                .findFirst();

        if (activeSubscription.isEmpty())
            throw new IllegalStateException("No active subscription found");

        OperationSchema operationSchema = organization.get().getOperationSchemas().
                stream()
                .filter(item -> item.getId().equals(schemaId))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalStateException("No schema found")
                );

        return Integer.parseInt(activeSubscription.get().getPlan().getFeatures().get("rules_per_schema")) > operationSchema.getRules().size() + 1;
    }

    @Override
    @Transactional
    public boolean isAddActionEligible(Long ruleId) {
        RuleEntity rule = ruleRepository.findById(ruleId)
                .orElseThrow(
                        () -> new IllegalStateException("No rule found")
                );

        Organization organization = rule.getOperationSchema().getOrganization();
        Optional<OrganizationSubscription> activeSubscription = organization.getSubscriptions()
                .stream()
                .filter(OrganizationSubscription::isActive)
                .findFirst();

        if (activeSubscription.isEmpty())
            throw new IllegalStateException("No active subscription found");

        return Integer.parseInt(activeSubscription.get().getPlan().getFeatures().get("actions_per_rule")) > rule.getActions().size() + 1;

    }

    public Bucket resolveBucket(String apiKey) {
        Optional<Organization> organization = organizationRepository.findByApiKey(apiKey);
        if (organization.isEmpty())
            throw new IllegalArgumentException("API Key not recognized");

        Optional<OrganizationSubscription> activeSubscription = organization.get().getSubscriptions()
                .stream()
                .filter(OrganizationSubscription::isActive)
                .findFirst();

        if (activeSubscription.isEmpty())
            throw new IllegalStateException("No active subscription found");

        return proxyManager.builder().build(apiKey, () ->
                newBucket(Integer.parseInt(activeSubscription.get().getPlan().getFeatures().get("requests_per_month")))
        );
    }

    private BucketConfiguration newBucket(int limits) {
        return BucketConfiguration.builder()
                .addLimit(
                        BandwidthBuilder.builder()
                                .capacity(limits)
                                .refillIntervally(limits, Duration.ofDays(1))
                                .build()
                )
                .build();
    }

}
