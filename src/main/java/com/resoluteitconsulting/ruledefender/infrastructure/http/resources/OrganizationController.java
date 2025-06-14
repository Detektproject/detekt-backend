package com.resoluteitconsulting.ruledefender.infrastructure.http.resources;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.resoluteitconsulting.ruledefender.domain.usecases.SubscriptionService;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.*;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.*;
import com.resoluteitconsulting.ruledefender.domain.model.OrganizationSummary;
import com.resoluteitconsulting.ruledefender.domain.model.OrganizationUser;
import com.resoluteitconsulting.ruledefender.domain.usecases.UserService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/organization")
@RequiredArgsConstructor
@Slf4j
public class OrganizationController {

    private static final String NO_ORGANIZATION_FOUND_ERROR = "No Organization found with the provided id";

    private final OrganizationRepository organizationRepository;
    private final OrganizationConfiguredEventRepository organizationConfiguredEventRepository;
    private final EventRepository eventRepository;
    private final OperationRepository operationRepository;
    private final UserOrganizationRepository userOrganizationRepository;
    private final EndpointRepository endpointRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final EntityManager entityManager;
    private final UserService userService;
    private final OrganizationSubscriptionRepository organizationSubscriptionRepository;
    private final SubscriptionService subscriptionService;


    @PostMapping
    @Transactional
    public ResponseEntity<Organization> createOrganisation(Authentication authentication, @RequestBody Organization organization) {

        Optional<UserOrganization> userOrganization = userOrganizationRepository.findByUserIdAndActivated(authentication.getName(), true);

        if (userOrganization.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not activated");

        log.info("Creating a new organization");
        organization.setApiKey(UUID.randomUUID().toString());

        organizationRepository.save(organization);

        userOrganization.get().setOrganization(organization);

        userOrganizationRepository.save(userOrganization.get());

        Endpoint endpoint = new Endpoint();
        endpoint.setName("Default");
        endpoint.setIsActive(true);
        endpoint.setOrganization(organization);

        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .replacePath(null)
                .replaceQuery(null)
                .fragment(null)
                .build()
                .toUriString();

        endpoint.setAddress(baseUrl + "/api/organization/" + organization.getId() + "/operation/v1/verify");

        endpoint.setDirection("IN");
        endpoint.setIsActive(true);
        endpoint.setDescription("Default organization HTTP Endpoint");
        endpoint.setType("HTTP");

        endpointRepository.save(endpoint);

        subscriptionService.activateDefaultSubscription(organization);

        return ResponseEntity.ok(organization);
    }


    @GetMapping("/{organizationId}")
    public ResponseEntity<Organization> getOrganisation(Authentication authentication, @PathVariable("organizationId") Long organizationId) {
        Optional<Organization> organization = organizationRepository.findById(organizationId);

        if (organization.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, NO_ORGANIZATION_FOUND_ERROR
            );

        return ResponseEntity.ok(organization.get());
    }


    @PutMapping("/{organizationId}")
    public ResponseEntity<Organization> updateOrganisation(Authentication authentication, @PathVariable("organizationId") Long organizationId,@RequestBody Organization organization) {
        Optional<Organization> organizationToUpdate = organizationRepository.findById(organizationId);

        if (organizationToUpdate.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, NO_ORGANIZATION_FOUND_ERROR
            );
        organizationToUpdate.get().setName(organization.getName());
        organizationToUpdate.get().setExternalId(organization.getExternalId());

        organizationRepository.save(organizationToUpdate.get());

        return ResponseEntity.ok(organizationToUpdate.get());
    }


    @DeleteMapping("/{organizationId}")
    public ResponseEntity<Void> deleteOrganisation(@PathVariable("organizationId") Long organizationId) {
        Optional<Organization> organizationToDelete = organizationRepository.findById(organizationId);

        if (organizationToDelete.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, NO_ORGANIZATION_FOUND_ERROR
            );

        organizationRepository.delete(organizationToDelete.get());
        return ResponseEntity.accepted().build();
    }


    @PutMapping("/{organizationId}/activate")
    public ResponseEntity<Void> activateOrganization(@PathVariable("organizationId") Long organizationId) {
        Optional<Organization> organizationToActivate = organizationRepository.findById(organizationId);

        if (organizationToActivate.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, NO_ORGANIZATION_FOUND_ERROR
            );
        organizationToActivate.get().setStatus(Boolean.TRUE);

        organizationRepository.save((organizationToActivate.get()));

        return ResponseEntity.accepted().build();
    }


    @PutMapping("/{organizationId}/suspend")
    public ResponseEntity<Void> suspendOrganisationStatus(@PathVariable("organizationId") Long organizationId) {
        Optional<Organization> organizationToSuspend = organizationRepository.findById(organizationId);

        if (organizationToSuspend.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, NO_ORGANIZATION_FOUND_ERROR
            );
        organizationToSuspend.get().setStatus(Boolean.FALSE);

        organizationRepository.save((organizationToSuspend.get()));

        return ResponseEntity.accepted().build();


    }


    @PostMapping("/{organizationId}/event-configuration")
    public ResponseEntity<OrganizationConfiguredEvent> addEventConfigToOrganization(Authentication authentication, @PathVariable("organizationId") Long organizationId, @RequestBody OrganizationConfiguredEvent eventConfiguration) {

        Optional<Organization> organization = organizationRepository.findById(organizationId);

        if (organization.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, NO_ORGANIZATION_FOUND_ERROR
            );
        OrganizationConfiguredEvent organizationConfiguredEvent = new OrganizationConfiguredEvent();
        organizationConfiguredEvent.setOrganizationId(organizationId);
        organizationConfiguredEvent.setEventConfigurationId(eventConfiguration.getEventConfigurationId());

        organizationConfiguredEventRepository.save(organizationConfiguredEvent);
        return ResponseEntity.ok(organizationConfiguredEvent);
    }


    @DeleteMapping("/{organizationId}/event-configuration")
    public ResponseEntity<OrganizationConfiguredEvent> removeEventConfigToOrganization(@PathVariable("organizationId") Long organizationId, @RequestBody OrganizationConfiguredEvent eventConfiguration) {

        Optional<Organization> organization = organizationRepository.findById(organizationId);

        if (organization.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, NO_ORGANIZATION_FOUND_ERROR
            );

        Optional<OrganizationConfiguredEvent> organizationConfiguredEventToDelete = organizationConfiguredEventRepository.findByEventConfigurationIdAndOrganizationId(eventConfiguration.getEventConfigurationId(), eventConfiguration.getOrganizationId());

        if (organizationConfiguredEventToDelete.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No Organization Configured Event found with the provided id"
            );

        organizationConfiguredEventRepository.delete(organizationConfiguredEventToDelete.get());
        return ResponseEntity.ok(organizationConfiguredEventToDelete.get());
    }


    @GetMapping("/{organizationId}/event-configuration")
    public ResponseEntity<List<OrganizationConfiguredEvent>> getOrganizationConfiguredEvents(@PathVariable("organizationId") Long organizationId) {
        return ResponseEntity.ok(organizationConfiguredEventRepository.findByOrganizationId(organizationId));
    }


    @GetMapping("/{organizationId}/event")
    public ResponseEntity<List<Event>> getEvents(@PathVariable("organizationId") Long organizationId) {
        return ResponseEntity.ok(eventRepository.findTop5ByOrganizationIdOrderByCreatedOnDesc(organizationId));
    }


    @GetMapping("/{organizationId}/summary")
    public ResponseEntity<OrganizationSummary> getOrganizationSummary(@PathVariable("organizationId") Long organizationId) {

        List<OrganizationSummaryDetail> organizationSummaryDetails = operationRepository.getOrganizationSummaryDetails(organizationId);
        OrganizationSummary organizationSummary = new OrganizationSummary();

        organizationSummary.setCurrentTotalOperations(
                organizationSummaryDetails.stream()
                        .filter(obj -> obj.getOperationDate().isEqual(LocalDate.now()))
                        .map(OrganizationSummaryDetail::getCount)
                        .reduce(0L, Long::sum)
        );
        organizationSummary.setPreviousTotalOperations(
                organizationSummaryDetails.stream()
                        .filter(obj -> obj.getOperationDate().isEqual(LocalDate.now().minusDays(1)))
                        .map(OrganizationSummaryDetail::getCount)
                        .reduce(0L, Long::sum)
        );

        organizationSummary.setCurrentTotalAnomalyOperations(
                organizationSummaryDetails.stream()
                        .filter(obj -> obj.getOperationDate().isEqual(LocalDate.now()) && obj.getIsAnomaly().equals("Y"))
                        .map(OrganizationSummaryDetail::getCount)
                        .reduce(0L, Long::sum)
        );

        organizationSummary.setPreviousTotalAnomalyOperations(
                organizationSummaryDetails.stream()
                        .filter(obj -> obj.getOperationDate().isEqual(LocalDate.now().minusDays(1)) && obj.getIsAnomaly().equals("Y"))
                        .map(OrganizationSummaryDetail::getCount)
                        .reduce(0L, Long::sum)
        );

        organizationSummary.setCurrentTotalNovelOperations(
                organizationSummaryDetails.stream()
                        .filter(obj -> obj.getOperationDate().isEqual(LocalDate.now()) && !obj.getIsAnomaly().equals("Y"))
                        .map(OrganizationSummaryDetail::getCount)
                        .reduce(0L, Long::sum)
        );

        organizationSummary.setPreviousTotalNovelOperations(
                organizationSummaryDetails.stream()
                        .filter(obj -> obj.getOperationDate().isEqual(LocalDate.now().minusDays(1)) && !obj.getIsAnomaly().equals("Y"))
                        .map(OrganizationSummaryDetail::getCount)
                        .reduce(0L, Long::sum)
        );


        organizationSummary.setTotalAnomalies(
                organizationSummaryDetails.stream()
                        .filter(obj -> obj.getIsAnomaly().equals("Y"))
                        .map(OrganizationSummaryDetail::getCount)
                        .reduce(0L, Long::sum)
        );

        organizationSummary.setTotalNovels(
                organizationSummaryDetails.stream()
                        .filter(obj -> !obj.getIsAnomaly().equals("Y"))
                        .map(OrganizationSummaryDetail::getCount)
                        .reduce(0L, Long::sum)
        );

        organizationSummaryDetails.sort(Comparator.comparing(OrganizationSummaryDetail::getOperationDate));

        return ResponseEntity.ok(organizationSummary);


    }

    @GetMapping("/{organizationId}/evolution")
    public ResponseEntity<List<Map<String, Object>>> getOrganizationEvolution(
            @PathVariable("organizationId") Long organizationId,
            @RequestParam(value = "isAnomaly", required = false) String isAnomaly,
            @RequestParam(value = "startDate", required = false) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) LocalDate endDate
    ) {

        QOperation operation = QOperation.operation;
        Predicate predicate = operation.organizationId.eq(organizationId);
        if (StringUtils.isNotBlank(isAnomaly)) {
            BooleanExpression operationByAnomalyStatus = operation.isAnomaly.eq(StringUtils.equals(isAnomaly, "Y"));
            predicate = operationByAnomalyStatus.and(predicate);
        }

        if (startDate != null) {
            BooleanExpression operationByStartDate = operation.operationDate.goe(startDate);
            predicate = operationByStartDate.and(predicate);
        }

        if (endDate != null) {
            BooleanExpression operationByEndDate = operation.operationDate.loe(endDate);
            predicate = operationByEndDate.and(predicate);
        }

        JPAQueryFactory qf = new JPAQueryFactory(entityManager);


        JPAQuery<Tuple> query = qf.from(operation)
                .where(predicate)
                .groupBy(operation.isAnomaly, operation.operationDate)
                .select(
                        Projections.tuple(
                                operation.isAnomaly,
                                operation.count().as("count"),
                                operation.operationDate
                        )
                );

        List<Tuple> organizationSummaryDetails = query.fetch();

        Map<LocalDate, List<Tuple>> groupedSummary = organizationSummaryDetails.stream()
                .collect(Collectors.groupingBy(item -> item.get(2, LocalDate.class)));

        List<Map<String, Object>> constructedSummaryDetail = new ArrayList<>();
        for (Map.Entry<LocalDate, List<Tuple>> entry : groupedSummary.entrySet()) {
            constructedSummaryDetail.add(
                    Map.of(
                            "date", entry.getKey(),
                            "anomaly", entry.getValue().stream().filter(item -> item.get(0, Boolean.class)).map(item -> item.get(1, Long.class)).reduce(0L, Long::sum),
                            "ordinary", entry.getValue().stream().filter(item -> !item.get(0, Boolean.class)).map(item -> item.get(1, Long.class)).reduce(0L, Long::sum)
                    )
            );
        }

        constructedSummaryDetail.sort(
                Comparator.comparing(
                        m -> (LocalDate) m.get("date"),
                        Comparator.nullsLast(Comparator.naturalOrder())
                )
        );

        return ResponseEntity.ok(constructedSummaryDetail);
    }

    @PostMapping("/{organizationId}/user")
    public ResponseEntity<Void> createUserAndLinkToOrganization(Authentication authentication, @PathVariable("organizationId") Long organizationId, @RequestBody OrganizationUser organizationUser){

        log.info("Creating a new user for organization={}", organizationId);

        Optional<Organization> organization = organizationRepository.findById(organizationId);
        if(organization.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No organization found with the provided id");

        String createdUserId = userService.createUserWithoutPassword(organizationUser);

        UserOrganization userOrganization = new UserOrganization();
        userOrganization.setUserId(createdUserId);
        userOrganization.setOrganization(organization.get());
        userOrganization.setStatus(true);
        userOrganization.setEmail(organizationUser.getEmail());
        userOrganizationRepository.save(userOrganization);

        return ResponseEntity.status(201).build();
    }

    @GetMapping("/{organizationId}/user")
    public ResponseEntity<PagedEntity<UserOrganization>> getUsers(Authentication authentication,
                                                                  @PathVariable(value = "organizationId") long organizationId,
                                                                  @RequestParam(value = "pageIndex", defaultValue = "1") int pageIndex,
                                                                  @RequestParam(value = "pageSize", defaultValue = "10") int pageSize
    ) {

        log.info("Getting users organization id={}", organizationId);

        Page<UserOrganization> userOrganizations = userOrganizationRepository.findByOrganizationId(organizationId, PageRequest.of(pageIndex-1, pageSize));

        return ResponseEntity.ok(
                new PagedEntity<UserOrganization>()
                        .pageIndex(pageIndex)
                        .pageCount(userOrganizations.getTotalPages())
                        .items(userOrganizations.getContent())
        );
    }

    @GetMapping("/user")
    public ResponseEntity<Organization> getOrganisationByAuthenticatedUser(Authentication authentication) {

        log.info("Getting organization details by userId:{}", authentication.getName());
        Optional<UserOrganization> userOrganization = userOrganizationRepository.findByUserIdAndActivated(authentication.getName(), Boolean.TRUE);

        if (userOrganization.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No user found with the provided id"
            );

        return ResponseEntity.ok(userOrganization.get().getOrganization());
    }

    @PutMapping("/{organizationId}/subscription")
    @Transactional
    public ResponseEntity<Organization> updateSubscription(
            Authentication authentication,
            @PathVariable(value = "organizationId") long organizationId,
            @RequestBody SubscriptionPlan subscriptionPlan) {

        subscriptionService.activateSubscription(organizationId, subscriptionPlan.getId());


        return ResponseEntity.accepted().build();
    }

}
