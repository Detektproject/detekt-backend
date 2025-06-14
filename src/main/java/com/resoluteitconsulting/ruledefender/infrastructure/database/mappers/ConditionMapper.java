package com.resoluteitconsulting.ruledefender.infrastructure.database.mappers;

import com.resoluteitconsulting.ruledefender.domain.model.Condition;
import com.resoluteitconsulting.ruledefender.domain.model.Criterion;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.ConditionEntity;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.CriterionEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConditionMapper {

    Condition entityToModel(ConditionEntity entity);

    List<Condition> entitiesToModels(List<ConditionEntity> entities);

    ConditionEntity modelToEntity(Condition model);

    List<ConditionEntity> modelsToEntities(List<Condition> models);

}
