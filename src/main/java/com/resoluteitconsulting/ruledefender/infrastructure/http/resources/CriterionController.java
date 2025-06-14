package com.resoluteitconsulting.ruledefender.infrastructure.http.resources;


import com.resoluteitconsulting.ruledefender.domain.model.Criterion;
import com.resoluteitconsulting.ruledefender.infrastructure.database.mappers.CriterionMapper;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.CriterionEntity;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.RuleEntity;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.CriterionRepository;
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
@RequestMapping("rule/{ruleId}/criteria")
@AllArgsConstructor
public class CriterionController {

    private final RuleRepository ruleRepository;
    private final CriterionRepository criterionRepository;
    private final CriterionMapper criterionMapper;

    @PostMapping
    public ResponseEntity<Criterion> createCriteria(
            Authentication authentication,
            @PathVariable("ruleId") Long ruleId,
            @RequestBody Criterion criterion) {

        Optional<RuleEntity> rule = ruleRepository.findById(ruleId);
        if (rule.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No rule found with the provided id"
            );

        CriterionEntity criterionEntity = criterionMapper.modelToEntity(criterion);
        criterionEntity.setRule(rule.get());
        criterionEntity.getConditions()
                        .forEach(
                                c -> c.setCriterion(criterionEntity)
                        );

        criterionRepository.save(criterionEntity);

        return ResponseEntity.ok(criterionMapper.entityToModel(criterionEntity));

    }


    @GetMapping("/{criteriaId}")
    public ResponseEntity<CriterionEntity> getCriteria(@PathVariable("criteriaId") Long criteriaId) {

        Optional<CriterionEntity> criteria = criterionRepository.findById(criteriaId);

        if (criteria.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No criteria found with the provided id"
            );

        return ResponseEntity.ok(criteria.get());

    }

    @GetMapping
    public ResponseEntity<List<Criterion>> getCriteriaList(@PathVariable("ruleId") Long ruleId) {
        List<CriterionEntity> criteria = criterionRepository.findByRuleId(ruleId);
        return ResponseEntity.ok(criterionMapper.entitiesToModels(criteria));
    }

    @PutMapping("/{criteriaId}")
    public ResponseEntity<Criterion> updateCriteria(
            Authentication authentication,
            @PathVariable("criteriaId") Long criteriaId,
            @RequestBody Criterion criterion) {

        Optional<CriterionEntity> criteriaToUpdate = criterionRepository.findById(criteriaId);

        if (criteriaToUpdate.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No criteria found with the provided id"
            );

        criterionMapper.updateEntityWithModel(criteriaToUpdate.get(), criterion);

        criteriaToUpdate.get().getConditions()
                .forEach(
                        c -> c.setCriterion(criteriaToUpdate.get())
                );

        criterionRepository.save(criteriaToUpdate.get());

        return ResponseEntity.ok(criterion);
    }

    @DeleteMapping("/{criteriaId}")
    public ResponseEntity<Void> deleteCriteria(@PathVariable("criteriaId") Long criteriaId) {
        Optional<CriterionEntity> criteria = criterionRepository.findById(criteriaId);

        if (criteria.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No criteria found with the provided id"
            );

        criterionRepository.delete(criteria.get());

        return ResponseEntity.accepted().build();
    }
}

