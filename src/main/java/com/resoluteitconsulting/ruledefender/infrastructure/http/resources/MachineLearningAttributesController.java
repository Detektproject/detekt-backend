package com.resoluteitconsulting.ruledefender.infrastructure.http.resources;


import com.resoluteitconsulting.ruledefender.infrastructure.database.model.MachineLearningAttributes;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.MachineLearningAttributesRepository;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.OperationSchemaRepository;
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

@RestController
@RequestMapping("/operation-schema/{operationSchemaId}/ml-attributes")
@AllArgsConstructor
public class MachineLearningAttributesController {

    private final MachineLearningAttributesRepository machineLearningAttributesRepository;
    private final OperationSchemaRepository operationSchemaRepository;

    @GetMapping
    public ResponseEntity<List<MachineLearningAttributes>> getMlAttributes(@PathVariable("operationSchemaId") Long operationSchemaId) {

        if (!operationSchemaRepository.existsById(operationSchemaId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Provided schema id is not found");

        List<MachineLearningAttributes> machineLearningAttributes = machineLearningAttributesRepository.findByOperationSchemaId(operationSchemaId);
        return ResponseEntity.ok(machineLearningAttributes);
    }

    @GetMapping("/{attributeId}")
    public ResponseEntity<MachineLearningAttributes> getMlAttribute(@PathVariable("operationSchemaId") Long operationSchemaId, @PathVariable("attributeId") Long attributeId) {

        if (!operationSchemaRepository.existsById(operationSchemaId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Provided schema id is not found");

        Optional<MachineLearningAttributes> machineLearningAttribute = machineLearningAttributesRepository.findById(attributeId);
        return ResponseEntity.of(machineLearningAttribute);
    }

    @PostMapping
    public ResponseEntity<MachineLearningAttributes> addMlAttribute(Authentication authentication, @PathVariable("operationSchemaId") Long operationSchemaId, @RequestBody MachineLearningAttributes machineLearningAttributes) {

        if (!operationSchemaRepository.existsById(operationSchemaId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Provided schema id is not found");

        Optional<MachineLearningAttributes> existingMachineLearning = machineLearningAttributesRepository.findByAttributeNameAndOperationSchemaId(machineLearningAttributes.getAttributeName(), operationSchemaId);

        if (existingMachineLearning.isPresent()) {

            existingMachineLearning.get().setAttributeType(machineLearningAttributes.getAttributeType());
            existingMachineLearning.get().setAttributeValuePath(machineLearningAttributes.getAttributeValuePath());
            existingMachineLearning.get().setIsActive(machineLearningAttributes.getIsActive());
            return ResponseEntity.ok(existingMachineLearning.get());

        } else {

            machineLearningAttributes.setOperationSchemaId(operationSchemaId);
            machineLearningAttributesRepository.save(machineLearningAttributes);
        }

        return ResponseEntity.ok(machineLearningAttributes);
    }

    @PutMapping("/{attributeId}")
    public ResponseEntity<MachineLearningAttributes> updateMlAttribute(Authentication authentication, @PathVariable("operationSchemaId") Long operationSchemaId, @PathVariable("attributeId") Long attributeId, @RequestBody MachineLearningAttributes machineLearningAttributes) {

        if (!operationSchemaRepository.existsById(operationSchemaId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Provided schema id is not found");

        Optional<MachineLearningAttributes> machineLearningAttributesToUpdate = machineLearningAttributesRepository.findById(attributeId);

        if (machineLearningAttributesToUpdate.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No attribute found with provided Id");


        machineLearningAttributesToUpdate.get().setAttributeName(machineLearningAttributes.getAttributeName());
        machineLearningAttributesToUpdate.get().setAttributeValuePath(machineLearningAttributes.getAttributeValuePath());
        machineLearningAttributesToUpdate.get().setIsActive(machineLearningAttributes.getIsActive());
        machineLearningAttributesToUpdate.get().setAttributeType(machineLearningAttributes.getAttributeType());

        machineLearningAttributesRepository.save(machineLearningAttributesToUpdate.get());

        return ResponseEntity.ok(machineLearningAttributesToUpdate.get());
    }

}
