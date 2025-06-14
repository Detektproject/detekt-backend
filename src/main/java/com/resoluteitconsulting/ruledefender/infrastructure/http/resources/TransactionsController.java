package com.resoluteitconsulting.ruledefender.infrastructure.http.resources;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.resoluteitconsulting.ruledefender.domain.model.FraudDetectionResult;
import com.resoluteitconsulting.ruledefender.domain.model.Rule;
import com.resoluteitconsulting.ruledefender.domain.model.Transaction;
import com.resoluteitconsulting.ruledefender.domain.usecases.FraudDetectorService;
import com.resoluteitconsulting.ruledefender.infrastructure.database.mappers.RuleMapper;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.*;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.*;
import com.resoluteitconsulting.ruledefender.domain.usecases.AiModelClassifier;
import com.resoluteitconsulting.ruledefender.domain.usecases.SubscriptionLimitingService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/organization/{organizationId}/operation")
@Slf4j
@AllArgsConstructor
public class TransactionsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsController.class);
    private static final Configuration JSON_PATH_CONF = Configuration.builder().options(Option.AS_PATH_LIST).build();


    private final OperationSchemaRepository operationSchemaRepository;
    private final OperationRepository operationRepository;
    private final ObjectMapper objectMapper;
    private final AiModelClassifier aiModelClassifier;
    private final OrganizationRepository organizationRepository;
    private final SubscriptionLimitingService subscriptionLimitingService;

    private final FraudDetectorService fraudDetectorService;
    private final RuleMapper ruleMapper;


    @PostMapping("v1/verify")
    @Transactional
    public ResponseEntity<FraudDetectionResult> verifyOperation(@RequestHeader("X-API-Key") String apiKey, @RequestHeader("X-Schema-Identifier") String operationSchemaIdentifier, @RequestBody String request) throws Exception {


        Optional<Organization> organization = organizationRepository.findByApiKey(apiKey);

        if (organization.isEmpty())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid API Key");

        if (Boolean.FALSE.equals(organization.get().getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your organization is temporary disabled");
        }

        Operation operation = new Operation();
        operation.setRequestContent(request);
        FraudDetectionResult detectionResult = processOperation(apiKey, operationSchemaIdentifier, operation);
        return ResponseEntity.ok(detectionResult);

    }

    @GetMapping
    public ResponseEntity<PagedEntity<Operation>> getOperations(
            @PathVariable("organizationId") Long organizationId,
            @RequestParam(value = "pageIndex", defaultValue = "0") int pageIndex,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "isAnomaly", required = false) String isAnomaly,
            @RequestParam(value = "startDate", required = false) Instant startDate,
            @RequestParam(value = "endDate", required = false) Instant endDate,
            @RequestParam(value = "requestKey", required = false) String requestKey

    ) {

        QOperation operation = QOperation.operation;

        Predicate predicate = operation.organizationId.eq(organizationId);
        if (StringUtils.isNotBlank(isAnomaly)) {
            BooleanExpression operationByAnomalyStatus = operation.isAnomaly.eq(StringUtils.equals(isAnomaly, "Y"));
            predicate = operationByAnomalyStatus.and(predicate);
        }

        if (startDate != null) {
            BooleanExpression operationByStartDate = operation.createdOn.goe(startDate);
            predicate = operationByStartDate.and(predicate);
        }

        if (endDate != null) {
            BooleanExpression operationByEndDate = operation.createdOn.loe(endDate);
            predicate = operationByEndDate.and(predicate);
        }

        if (StringUtils.isNotBlank(requestKey)) {
            BooleanExpression requestKeyExpression = operation.requestKey.eq(requestKey);
            predicate = requestKeyExpression.and(predicate);
        }

        Page<Operation> operationPage = operationRepository.findAll(predicate, PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "receivedAt")));

        return ResponseEntity.ok(
                new PagedEntity<Operation>()
                        .items(operationPage.getContent())
                        .pageIndex(pageIndex)
                        .pageCount(operationPage.getTotalPages())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Operation> getOperationById(@PathVariable("id") Long id) {

        Optional<Operation> operation = operationRepository.findById(id);

        return ResponseEntity.of(operation);

    }

    @PutMapping("/{id}")
    public ResponseEntity<Operation> updateOperationStatus(Authentication authentication, @PathVariable("id") Long id, @RequestBody Operation operation) {
        LOGGER.info("Update operation {} with new status={}", id, operation.getIsAnomaly());

        Optional<Operation> operationToUpdate = operationRepository.findById(id);

        if (operationToUpdate.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No operation found with the provided id"
            );

        operationToUpdate.get().setIsAnomaly(operation.getIsAnomaly());

        operationRepository.save(operationToUpdate.get());

        return ResponseEntity.ok(operationToUpdate.get());
    }


    public FraudDetectionResult processOperation(String apiKey, String schemaIdentifier, Operation operation) throws Exception {

        LOGGER.info("Finding Schema by Schema Id: {}", schemaIdentifier);

        if (!subscriptionLimitingService.isProcessOperationEligible(apiKey)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "You've reached your daily limit");
        }

        Optional<OperationSchema> schema = operationSchemaRepository.findBySchemaIdentifier(schemaIdentifier);
        if (schema.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No schema found with the provided id"
            );

        LOGGER.info("Schema with Id={} will be used to verify this operation, Organization Id={}", schema.get().getId(), schema.get().getOrganizationId());
        String requestKey = getRequestKey(operation, schema.get());
        LOGGER.info("Getting operation list by schema={} and key={}", schema.get().getId(), requestKey);


        operation.setReceivedAt(Instant.now());
        operation.setOperationDate(LocalDate.now());
        operation.setOperationSchema(schema.get());
        operation.setOperationSchemaId(schema.get().getId());
        operation.setOrganization(schema.get().getOrganization());
        operation.setOrganizationId(schema.get().getOrganization().getId());
        operation.setRequestKey(requestKey);

        List<Operation> operations = operationRepository.findByRequestKeyAndOperationSchemaId(requestKey, schema.get().getId());

        LOGGER.info("Rules will be applied on the previous operations");
        List<Rule> detectedRules = fraudDetectorService.evaluate(
                new Transaction(JsonPath.parse(operation.getRequestContent()), Instant.now()),
                operations.stream().map(
                        item -> new Transaction(JsonPath.parse(item.getRequestContent()), item.getReceivedAt())
                ).collect(Collectors.toList()),
                ruleMapper.entitiesToModels(
                        schema.get().getRules().stream().toList()
                )
        );

        Set<DetectedOperationRule> detectedOperationRules = detectedRules
                .stream()
                .map(rule -> {
                            DetectedOperationRule detectedRule = new DetectedOperationRule();
                            detectedRule.setOperation(operation);
                            detectedRule.setRule(schema.get().getRules().stream().filter(r -> Objects.equals(r.getId(), rule.id())).findFirst().orElseThrow());
                            return detectedRule;
                        }

                ).collect(Collectors.toSet());

        operation.setDetectedRules(detectedOperationRules);
        operation.setIsAnomaly(!detectedRules.isEmpty());
        aiModelClassifier.loadIfNecessary(operation.getOperationSchemaId());
        Map<String, String> score = aiModelClassifier.classify(operation);
        operation.setPredictionResult(score.get("result"));
        operation.setPredictionScore(score.get("score"));

        operationRepository.saveAndFlush(operation);

        double totalScore = detectedRules.stream()
                .mapToDouble(Rule::weight)
                .sum();
        double totalWeight = schema.get().getRules().stream()
                .mapToDouble(r -> Double.parseDouble(r.getWeight()))
                .sum();
        double fraudScore = totalScore / totalWeight;


        return new FraudDetectionResult(
                fraudScore,
                score.get("result"),
                score.get("score")
        );
    }

    private String getRequestKey(Operation operation, OperationSchema operationSchema) {
        DocumentContext jsonContext = JsonPath.parse(operation.getRequestContent());
        return jsonContext.read(operationSchema.getPath());
    }


}
