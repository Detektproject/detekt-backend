package com.resoluteitconsulting.ruledefender.infrastructure.database.mappers;

import com.resoluteitconsulting.ruledefender.domain.model.Criterion;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.CriterionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ConditionMapper.class})
public interface CriterionMapper {

    Criterion entityToModel(CriterionEntity entity);

    List<Criterion> entitiesToModels(List<CriterionEntity> entities);

    void updateEntityWithModel(@MappingTarget CriterionEntity source, Criterion criterion);

    CriterionEntity modelToEntity(Criterion model);

    List<CriterionEntity> modelsToEntities(List<Criterion> models);

}
