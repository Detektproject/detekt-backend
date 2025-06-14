package com.resoluteitconsulting.ruledefender.infrastructure.database.mappers;

import com.resoluteitconsulting.ruledefender.domain.model.Criterion;
import com.resoluteitconsulting.ruledefender.domain.model.Rule;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.CriterionEntity;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.RuleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CriterionMapper.class})
public interface RuleMapper {

    Rule entityToModel(RuleEntity entity);

    List<Rule> entitiesToModels(List<RuleEntity> entities);

    void updateEntityWithModel(@MappingTarget RuleEntity source, Rule criterion);

    RuleEntity modelToEntity(Rule model);

    List<RuleEntity> modelsToEntities(List<Rule> models);

}
