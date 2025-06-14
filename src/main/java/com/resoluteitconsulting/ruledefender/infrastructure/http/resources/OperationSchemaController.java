package com.resoluteitconsulting.ruledefender.infrastructure.http.resources;

import com.resoluteitconsulting.ruledefender.domain.usecases.SubscriptionLimitingService;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.OperationSchema;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.Organization;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.PagedEntity;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.OperationSchemaRepository;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.OrganizationRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/organization/{organizationId}/operation-schema")
@AllArgsConstructor
public class OperationSchemaController {
    private static final Logger LOGGER = LoggerFactory.getLogger(OperationSchemaController.class);

    private OperationSchemaRepository operationSchemaRepository;
    private OrganizationRepository organizationRepository;
    private SubscriptionLimitingService subscriptionLimitingService;

    @PostMapping
    public ResponseEntity<OperationSchema> createOperationSchema(Authentication authentication, @PathVariable("organizationId") Long organizationId, @RequestBody OperationSchema operationSchema) {
        LOGGER.info("Creating new operation schema for organization={}", organizationId);

        Optional<Organization> organization = organizationRepository.findById(organizationId);

        if (organization.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No organization found with the provided id"
            );

        if(!subscriptionLimitingService.isAddSchemaEligible(organization.get().getApiKey()))
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS, "You've reached your limit"
            );

        operationSchema.setOrganization(organization.get());
        operationSchema.setSchemaIdentifier(UUID.randomUUID().toString());

        operationSchemaRepository.save(operationSchema);


        return ResponseEntity.ok(operationSchema);

    }

    @GetMapping
    public ResponseEntity<PagedEntity<OperationSchema>> getOperationSchemas(
            @PathVariable("organizationId") Long organizationId,
            @RequestParam(value = "pageIndex", defaultValue = "0") int pageIndex,
            @RequestParam(value = "pageSize", defaultValue = "1O") int pageSize,
            @RequestParam(value = "isAIEnabled", required = false) Boolean isAIEnabled
    ) {

        Optional<Organization> organization = organizationRepository.findById(organizationId);

        if (organization.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No organization found with the provided id"
            );
        Page<OperationSchema> operationSchemaPage;
        if(Objects.nonNull(isAIEnabled) && Boolean.TRUE.equals(isAIEnabled)){
            operationSchemaPage = operationSchemaRepository.findByOrganizationIdAndIsAIEnabled(organizationId, true, PageRequest.of(pageIndex, pageSize));
        }else {
            operationSchemaPage = operationSchemaRepository.findByOrganizationId(organizationId, PageRequest.of(pageIndex, pageSize));
        }

        return ResponseEntity.ok(
                new PagedEntity<OperationSchema>()
                        .items(operationSchemaPage.getContent())
                        .pageCount(operationSchemaPage.getTotalPages())
                        .pageIndex(pageIndex)
        );

    }

    @GetMapping("/{operationSchemaId}")
    public ResponseEntity<OperationSchema> getOperationSchema(@PathVariable("organizationId") Long organizationId, @PathVariable("operationSchemaId") Long operationSchemaId) {
        Optional<Organization> organization = organizationRepository.findById(organizationId);

        if (organization.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No organization found with the provided id"
            );

        Optional<OperationSchema> operationSchema = operationSchemaRepository.findById(operationSchemaId);

        return ResponseEntity.of(operationSchema);
    }

    @PutMapping("/{operationSchemaId}")
    public ResponseEntity<OperationSchema> updateOperationSchema(Authentication authentication, @PathVariable("organizationId") Long organizationId, @PathVariable("operationSchemaId") Long operationSchemaId, @RequestBody OperationSchema operationSchema) {

        Optional<Organization> organization = organizationRepository.findById(organizationId);

        if (organization.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No organization found with the provided id"
            );

        Optional<OperationSchema> operationSchemaToUpdate = operationSchemaRepository.findById(operationSchemaId);

        if (operationSchemaToUpdate.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No schema found with the provided id"
            );

        operationSchemaToUpdate.get().setSchemaKey(operationSchema.getSchemaKey());
        operationSchemaToUpdate.get().setName(operationSchema.getName());
        operationSchemaToUpdate.get().setPath(operationSchema.getPath());
        operationSchemaToUpdate.get().setValue(operationSchema.getValue());
        operationSchemaToUpdate.get().setIsAIEnabled(operationSchema.getIsAIEnabled());
        operationSchemaToUpdate.get().setType(operationSchema.getType());
        operationSchemaToUpdate.get().setDateIncluded(operationSchema.isDateIncluded());
        operationSchemaToUpdate.get().setDateFormat(operationSchema.getDateFormat());
        operationSchemaToUpdate.get().setDatePath(operationSchema.getDatePath());

        operationSchemaRepository.save(operationSchemaToUpdate.get());

        return ResponseEntity.of(operationSchemaToUpdate);
    }

    @DeleteMapping("/{operationSchemaId}")
    public ResponseEntity<Void> deleteOperationSchema(@PathVariable("organizationId") Long organizationId, @PathVariable("operationSchemaId") Long operationSchemaId) {

        Optional<Organization> organization = organizationRepository.findById(organizationId);

        if (organization.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No organization found with the provided id"
            );

        Optional<OperationSchema> operationSchema = operationSchemaRepository.findById(operationSchemaId);

        if (operationSchema.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No schema found with the provided id"
            );

        operationSchemaRepository.deleteById(operationSchemaId);

        return ResponseEntity.accepted().build();
    }

}
