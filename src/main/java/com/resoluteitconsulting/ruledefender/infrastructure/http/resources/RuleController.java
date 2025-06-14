package com.resoluteitconsulting.ruledefender.infrastructure.http.resources;

import com.resoluteitconsulting.ruledefender.domain.usecases.SubscriptionLimitingService;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.OperationSchema;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.PagedEntity;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.RuleEntity;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/operation-schema/{operationSchemaId}/rule")
@Slf4j
@AllArgsConstructor
public class RuleController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleController.class);
    public static final String NO_RULE_FOUND_WITH_THE_PROVIDED_ID = "No rule found with the provided id";

    private final RuleRepository ruleRepository;
    private final OperationSchemaRepository operationSchemaRepository;
    private final EndpointRepository endpointRepository;
    private final ActionRepository actionRepository;
    private final SubscriptionLimitingService subscriptionLimitingService;

    @PostMapping
    public ResponseEntity<RuleEntity> createRule(Authentication authentication, @PathVariable("operationSchemaId") Long operationSchemaId, @RequestBody RuleEntity rule) {

        Optional<OperationSchema> operationSchema = operationSchemaRepository.findById(operationSchemaId);

        if (operationSchema.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No schema found with provided Id");

        if(!subscriptionLimitingService.isAddRuleEligible(operationSchema.get().getOrganization().getApiKey(), operationSchemaId))
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS, "You've reached your limit"
            );

        rule.setOperationSchema(operationSchema.get());
        ruleRepository.save(rule);

        return ResponseEntity.ok(rule);
    }

    @GetMapping
    public ResponseEntity<PagedEntity<RuleEntity>> getRules(
            @PathVariable("operationSchemaId") Long operationSchemaId,
            @RequestParam(value = "pageIndex", defaultValue = "0") int pageIndex,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        Page<RuleEntity> rulePage = ruleRepository.findByOperationSchemaId(operationSchemaId, PageRequest.of(pageIndex, pageSize));
        return ResponseEntity.ok(
                new PagedEntity<RuleEntity>()
                        .items(rulePage.getContent())
                        .pageCount(rulePage.getTotalPages())
                        .pageIndex(pageIndex)
        );
    }


    @GetMapping("/{ruleId}")
    public ResponseEntity<RuleEntity> getRule(@PathVariable("operationSchemaId") Long operationSchemaId, @PathVariable("ruleId") Long ruleId) {
        LOGGER.info("Getting rule with operationSchemaId={} and ruleId={}", operationSchemaId, ruleId);

        Optional<RuleEntity> rule = ruleRepository.findById(ruleId);

        if (rule.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, NO_RULE_FOUND_WITH_THE_PROVIDED_ID
            );

        return ResponseEntity.ok(rule.get());
    }


    @PutMapping("/{ruleId}")
    @Transactional
    public ResponseEntity<RuleEntity> updateRule(Authentication authentication, @PathVariable("operationSchemaId") Long operationSchemaId, @PathVariable("ruleId") Long ruleId, @RequestBody RuleEntity rule) {

        Optional<RuleEntity> ruleToUpdate = ruleRepository.findById(ruleId);

        if (ruleToUpdate.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, NO_RULE_FOUND_WITH_THE_PROVIDED_ID
            );

        ruleToUpdate.get().setName(rule.getName());
        ruleToUpdate.get().setDescription(rule.getDescription());
        ruleToUpdate.get().setIntervalType(rule.getIntervalType());
        ruleToUpdate.get().setIntervalValue(rule.getIntervalValue());
        ruleToUpdate.get().setStartDate(rule.getStartDate());
        ruleToUpdate.get().setEndDate(rule.getEndDate());

        ruleRepository.save(ruleToUpdate.get());
        return ResponseEntity.ok(ruleToUpdate.get());


    }

    private void updateActions(Authentication authentication, RuleEntity rule, RuleEntity ruleToUpdate) {
        if (rule.getActions() != null && !rule.getActions().isEmpty()) {
            rule.getActions().forEach(
                    action -> {
                        if (Objects.isNull(action.getId())) {

                            endpointRepository.findById(action.getEndpointId())
                                    .ifPresent(
                                            endpoint -> {
                                                action.setEndpoint(endpoint);
                                                ruleToUpdate.getActions().add(action);
                                            }
                                    );


                        } else {

                            endpointRepository.findById(action.getEndpointId())
                                    .ifPresent(
                                            endpoint -> ruleToUpdate.getActions().stream().filter(a -> Objects.equals(a.getId(), action.getId()))
                                                    .findFirst()
                                                    .ifPresent(
                                                            actionToUpdate -> {
                                                                actionToUpdate.setName(action.getName());
                                                                actionToUpdate.setIsActive(action.getIsActive());
                                                                actionToUpdate.setValue(action.getValue());
                                                                actionToUpdate.setEndpoint(endpoint);
                                                            }
                                                    )
                                    );

                        }
                    }
            );


            ruleToUpdate.getActions().stream()
                    .filter(action -> rule.getActions().stream().noneMatch(item -> Objects.equals(item.getId(), action.getId())))
                    .forEach(
                            action -> {
                                log.info("Deleting action {}", action.getName());
                                actionRepository.deleteById(action.getId());
                            }
                    );

            ruleToUpdate.getActions().removeIf(
                    action -> rule.getActions().stream().noneMatch(item -> Objects.equals(item.getId(), action.getId()))
            );


        } else {
            if (!ruleToUpdate.getActions().isEmpty()) {
                ruleToUpdate.getActions().forEach(
                        action -> actionRepository.deleteById(action.getId())
                );
            }

            ruleToUpdate.getActions().removeIf(
                    action -> rule.getActions().stream().noneMatch(item -> Objects.equals(item.getId(), action.getId()))
            );

        }
    }



    @DeleteMapping("/{ruleId}")
    public ResponseEntity<Void> deleteRule(@PathVariable("ruleId") Long ruleId) {
        Optional<RuleEntity> ruleToDelete = ruleRepository.findById(ruleId);

        if (ruleToDelete.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, NO_RULE_FOUND_WITH_THE_PROVIDED_ID
            );
        ruleRepository.delete(ruleToDelete.get());
        return ResponseEntity.accepted().build();
    }
}
