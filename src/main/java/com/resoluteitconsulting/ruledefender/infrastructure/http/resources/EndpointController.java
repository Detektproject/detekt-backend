package com.resoluteitconsulting.ruledefender.infrastructure.http.resources;


import com.resoluteitconsulting.ruledefender.domain.usecases.SubscriptionLimitingService;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.Endpoint;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.Organization;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.EndpointRepository;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.OrganizationRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@RequestMapping("/organization/{organizationId}/endpoint")
@RestController
@AllArgsConstructor
public class EndpointController {

    private OrganizationRepository organizationRepository;
    private EndpointRepository endpointRepository;
    private SubscriptionLimitingService subscriptionLimitingService;

    @PostMapping
    public ResponseEntity<Endpoint> addEndpoint(Authentication authentication, @PathVariable("organizationId") Long organizationId, @RequestBody Endpoint endpoint) {



        Optional<Organization> organization = organizationRepository.findById(organizationId);

        if(organization.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No organization found with the provided id"
            );

        if(!subscriptionLimitingService.isAddEndpointEligible(organization.get().getApiKey()))
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS, "You've reached your limit"
            );

        endpoint.setOrganization(organization.get());

        endpointRepository.save(endpoint);

        return ResponseEntity.ok(endpoint);

    }

    @GetMapping
    public ResponseEntity<List<Endpoint>> getEndpoints(@PathVariable("organizationId") Long organizationId) {

        Optional<Organization> organization = organizationRepository.findById(organizationId);

        if(organization.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No organization found with the provided id"
            );

        List<Endpoint> endpoints = endpointRepository.findByOrganizationId(organizationId);

        return ResponseEntity.ok(endpoints);
    }

    @GetMapping("/{endpointId}")
    public ResponseEntity<Endpoint> getEndpoint(@PathVariable("organizationId") Long organizationId, @PathVariable("endpointId") Long endpointId) {

        Optional<Organization> organization = organizationRepository.findById(organizationId);

        if(organization.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No organization found with the provided id"
            );

        Optional<Endpoint> endpoint = endpointRepository.findById(endpointId);

        if(endpoint.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No endpoint found with the provided id"
            );

        return ResponseEntity.ok(endpoint.get());
    }

    @PutMapping("/{endpointId}")
    public ResponseEntity<Endpoint> updateEndpoint(Authentication authentication,@PathVariable("organizationId") Long organizationId, @PathVariable("endpointId") Long endpointId, @RequestBody Endpoint endpoint) {

        Optional<Organization> organization = organizationRepository.findById(organizationId);

        if(organization.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No organization found with the provided id"
            );

        Optional<Endpoint> endpointToUpdate = endpointRepository.findById(endpointId);

        if(endpointToUpdate.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No endpoint found with the provided id"
            );

        endpointToUpdate.get().setName(endpoint.getName());
        endpointToUpdate.get().setIsActive(endpoint.getIsActive());
        endpointToUpdate.get().setAddress(endpoint.getAddress());
        endpointToUpdate.get().setCertificate(endpoint.getCertificate());
        endpointToUpdate.get().setDescription(endpoint.getDescription());
        endpointToUpdate.get().setDirection(endpoint.getDirection());
        endpointToUpdate.get().setType(endpoint.getType());

        endpointRepository.save(endpointToUpdate.get());

        return ResponseEntity.ok(endpointToUpdate.get());

    }

}
