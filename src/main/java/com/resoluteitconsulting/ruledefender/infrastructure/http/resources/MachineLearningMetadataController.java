package com.resoluteitconsulting.ruledefender.infrastructure.http.resources;

import com.resoluteitconsulting.ruledefender.infrastructure.database.model.MachineLearningAttributes;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.MachineLearningMetadata;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.OperationSchema;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.PagedEntity;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.MachineLearningAttributesRepository;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.MachineLearningMetadataRepository;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.OperationSchemaRepository;
import com.resoluteitconsulting.ruledefender.domain.usecases.AiModelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RestController
@Slf4j
public class MachineLearningMetadataController {

    @Autowired
    private OperationSchemaRepository operationSchemaRepository;

    @Autowired
    private MachineLearningAttributesRepository machineLearningAttributesRepository;

    @Autowired
    private MachineLearningMetadataRepository machineLearningMetadataRepository;

    @Autowired
    private AiModelBuilder aiModelBuilder;

    @GetMapping("/organization/{organizationId}/ml-metadata")
    public ResponseEntity<PagedEntity<MachineLearningMetadata>> getMlMetadata(
            @PathVariable("organizationId") long organizationId,
            @PathVariable(value = "schemaId", required = false) Long schemaId,
            @RequestParam(value = "pageIndex", defaultValue = "0") int pageIndex,
            @RequestParam(value = "pageSize", defaultValue = "1O") int pageSize
    ) {

        Page<MachineLearningMetadata> machineLearningMetadataPage;
        if (Objects.nonNull(schemaId)) {
            machineLearningMetadataPage = machineLearningMetadataRepository.findByOperationSchemaId(schemaId, PageRequest.of(pageIndex, pageSize));
        } else {
            machineLearningMetadataPage = machineLearningMetadataRepository.findAll(PageRequest.of(pageIndex, pageSize));
        }

        return ResponseEntity.ok(
                new PagedEntity<MachineLearningMetadata>()
                        .items(machineLearningMetadataPage.getContent())
                        .pageIndex(pageIndex)
                        .pageCount(machineLearningMetadataPage.getTotalPages())
        );

    }

    @PostMapping("/organization/{organizationId}/ml-model")
    public ResponseEntity<Void> buildModel(Authentication authentication, @PathVariable("organizationId") Long organizationId, @RequestParam("operationSchemaId") Long schemaId) {
        log.info("Building Model for Organization Id:{} and Schema Id:{}", organizationId, schemaId);

        Optional<OperationSchema> operationSchema = operationSchemaRepository.findById(schemaId);
        if (operationSchema.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No Schema found with provided Id");

        if (Boolean.FALSE.equals(operationSchema.get().getIsAIEnabled()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "AI is not enabled for the provided schema");


        List<MachineLearningAttributes> machineLearningAttributes = machineLearningAttributesRepository.findByOperationSchemaId(schemaId);

        if (machineLearningAttributes.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No attribute found, please set attributes before building the model");

        launchModelBuilder(authentication.getName(), schemaId);
        return ResponseEntity.accepted().build();
    }

    @Async
    private void launchModelBuilder(String launchedBy, Long operationSchemaId) {

        if (machineLearningMetadataRepository.existsByOperationSchemaIdAndStatus(operationSchemaId, "P")) {
            log.warn("An ongoing build is running, this call will ignored");
            return;
        }

        MachineLearningMetadata machineLearningMetadata = new MachineLearningMetadata();

        String buildId = UUID.randomUUID().toString();

        machineLearningMetadata.setId(buildId);
        machineLearningMetadata.setOperationSchemaId(operationSchemaId);
        machineLearningMetadata.setStatus("P");
        machineLearningMetadata.setActive(false);
        machineLearningMetadataRepository.saveAndFlush(machineLearningMetadata);


        try {
            List<Double> buildResult = aiModelBuilder.trainModel(buildId, operationSchemaId);
            machineLearningMetadata.setAccuracy(buildResult.get(0));
            machineLearningMetadata.setDataSize(buildResult.get(1).intValue());

            machineLearningMetadata.setStatus("S");
            machineLearningMetadata.setActive(true);

        } catch (Exception ex) {
            log.error("Exception occurred while building the model", ex);
            machineLearningMetadata.setActive(false);
            machineLearningMetadata.setStatus("F");
        }

        machineLearningMetadata.setLastBuildTime(OffsetDateTime.now());
        machineLearningMetadataRepository.saveAndFlush(machineLearningMetadata);


    }

}
