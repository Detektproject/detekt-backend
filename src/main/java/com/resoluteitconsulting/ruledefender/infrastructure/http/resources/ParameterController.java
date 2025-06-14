package com.resoluteitconsulting.ruledefender.infrastructure.http.resources;

import com.resoluteitconsulting.ruledefender.infrastructure.database.model.PagedEntity;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.ParameterEntity;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.ParameterEntityRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/organization/{organizationId}/parameters")
@AllArgsConstructor
public class ParameterController {

    private ParameterEntityRepository parameterEntityRepository;

    @GetMapping
    public ResponseEntity<PagedEntity<ParameterEntity>> getParameters(
            Authentication authentication,
            @PathVariable("organizationId") Long organizationId,
            @RequestParam(value = "pageIndex", defaultValue = "0") int pageIndex,
            @RequestParam(value = "pageSize", defaultValue = "1O") int pageSize

    ) {

        Page<ParameterEntity> pagedEntity = parameterEntityRepository.findByOrganizationId(
                organizationId,
                PageRequest.of(pageIndex, pageSize)
        );

        return ResponseEntity.ok(
                new PagedEntity<ParameterEntity>()
                        .items(pagedEntity.getContent())
                        .pageCount(pagedEntity.getTotalPages())
                        .pageIndex(pageIndex)
        );
    }

    @PostMapping
    public ResponseEntity<ParameterEntity> addParameters(
            Authentication authentication,
            @PathVariable("organizationId") Long organizationId,
            @RequestBody ParameterEntity parameterEntity
    ) {

        parameterEntityRepository.save(parameterEntity);
        return ResponseEntity.ok(parameterEntity);
    }

    @DeleteMapping("/{parameterId}")
    public ResponseEntity<ParameterEntity> deleteParameters(
            Authentication authentication,
            @PathVariable("organizationId") Long organizationId,
            @PathVariable("parameterId") Long parameterId
    ) {

        ParameterEntity parameterEntity = parameterEntityRepository.findById(parameterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No parameter found with provided ID"));

        parameterEntityRepository.delete(parameterEntity);

        return ResponseEntity.accepted().build();
    }
}
