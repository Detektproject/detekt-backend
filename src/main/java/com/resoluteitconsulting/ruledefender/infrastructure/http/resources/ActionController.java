package com.resoluteitconsulting.ruledefender.infrastructure.http.resources;


import com.resoluteitconsulting.ruledefender.domain.usecases.SubscriptionLimitingService;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.Action;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.Endpoint;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.RuleEntity;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.ActionRepository;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.EndpointRepository;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.RuleRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("rule/{ruleId}/action")
@AllArgsConstructor
public class ActionController {

    public static final String NO_ACTION_FOUND_WITH_THE_PROVIDED_ID = "No action found with the provided id";
    private final EndpointRepository endpointRepository;
    private final RuleRepository ruleRepository;
    private final ActionRepository actionRepository;
    private final SubscriptionLimitingService subscriptionLimitingService;

    @PostMapping
    public Action createAction(Authentication authentication, @PathVariable("ruleId") Long ruleId, @RequestBody Action action) {

        Optional<RuleEntity> rule = ruleRepository.findById(ruleId);

        if(rule.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No rule found with the provided id"
            );


        if(!subscriptionLimitingService.isAddActionEligible(ruleId))
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS, "You've reached your limit"
            );

        Optional<Endpoint> endpoint = endpointRepository.findById(action.getEndpointId());

        if (endpoint.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No endpoint found with the provided id"
            );

        action.setRule(rule.get());
        action.setEndpoint(endpoint.get());



        return actionRepository.save(action);

    }

    @GetMapping
    public List<Action> getActionsByRuleId(@PathVariable("ruleId") Long ruleId) {

        Optional<RuleEntity> rule = ruleRepository.findById(ruleId);

        if(rule.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No rule found with the provided id"
            );

        return actionRepository.findByRuleId(ruleId);
    }


    @GetMapping("/{actionId}")
    public Action getActionById(@PathVariable("actionId") Long actionId) {

        Optional<Action> action = actionRepository.findById(actionId);

        if(action.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, NO_ACTION_FOUND_WITH_THE_PROVIDED_ID
            );

        return action.get();
    }

    @PutMapping("/{actionId}")
    public ResponseEntity<Action> updateAction(Authentication authentication,@PathVariable("actionId") Long actionId, Action action) {

        Optional<Action> actionToUpdate = actionRepository.findById(actionId);

        if(actionToUpdate.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, NO_ACTION_FOUND_WITH_THE_PROVIDED_ID
            );

        actionToUpdate.get().setName(action.getName());
        actionToUpdate.get().setIsActive(action.getIsActive());
        actionToUpdate.get().setValue(action.getValue());
        Optional<Endpoint> endpoint = endpointRepository.findById(action.getEndpointId());

        if (endpoint.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No endpoint found with the provided id"
            );

        actionToUpdate.get().setEndpoint(endpoint.get());

        actionRepository.save(actionToUpdate.get());

        return ResponseEntity.ok(actionToUpdate.get());
    }

    @DeleteMapping("/{actionId}")
    public ResponseEntity<Void> deleteAction(@PathVariable("actionId") Long actionId) {
        Optional<Action> actionToUpdate = actionRepository.findById(actionId);

        if(actionToUpdate.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, NO_ACTION_FOUND_WITH_THE_PROVIDED_ID
            );

        actionRepository.delete(actionToUpdate.get());

        return ResponseEntity.accepted().build();
    }
}
