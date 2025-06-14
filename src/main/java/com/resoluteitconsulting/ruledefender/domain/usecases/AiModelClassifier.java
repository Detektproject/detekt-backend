package com.resoluteitconsulting.ruledefender.domain.usecases;


import com.resoluteitconsulting.ruledefender.infrastructure.database.model.Operation;

import java.util.Map;

public interface AiModelClassifier {

    void loadIfNecessary(Long schemaId) throws Exception;

    Map<String, String> classify(Operation operation) throws Exception;

}
