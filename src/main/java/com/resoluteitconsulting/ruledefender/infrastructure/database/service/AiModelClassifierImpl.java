package com.resoluteitconsulting.ruledefender.infrastructure.database.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.MachineLearningAttributes;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.MachineLearningMetadata;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.Operation;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.MachineLearningAttributesRepository;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.MachineLearningMetadataRepository;
import com.resoluteitconsulting.ruledefender.domain.usecases.AiModelBuilder;
import com.resoluteitconsulting.ruledefender.domain.usecases.AiModelClassifier;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.tribuo.Example;
import org.tribuo.Model;
import org.tribuo.classification.Label;
import org.tribuo.classification.LabelFactory;
import org.tribuo.data.columnar.RowProcessor;
import org.tribuo.data.columnar.processors.field.DoubleFieldProcessor;
import org.tribuo.data.columnar.processors.field.IdentityProcessor;
import org.tribuo.data.columnar.processors.field.TextFieldProcessor;
import org.tribuo.data.columnar.processors.response.FieldResponseProcessor;
import org.tribuo.data.text.impl.BasicPipeline;
import org.tribuo.util.tokens.impl.BreakIteratorTokenizer;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiModelClassifierImpl implements AiModelClassifier {

    private final Queue<MachineLearningBuildComponent> loadingMap = new ConcurrentLinkedQueue<>();

    private final AiModelBuilder aiModelBuilder;
    private final MachineLearningAttributesRepository machineLearningAttributesRepository;
    private final MachineLearningMetadataRepository machineLearningMetadataRepository;
    private final ObjectMapper objectMapper;


    @Override
    public void loadIfNecessary(Long schemaId) throws IOException, ClassNotFoundException {

        Optional<MachineLearningMetadata> lastBuild = machineLearningMetadataRepository.findFirstByStatusAndIsActiveAndOperationSchemaIdOrderByLastBuildTimeDesc(
                "S",
                true,
                schemaId
        );

        if (lastBuild.isEmpty()) {
            log.warn("No build has been found for this schema");
            return;
        }

        Optional<MachineLearningBuildComponent> machineLearningBuildComponent = loadingMap.stream().filter(
                item -> item.getSchemaId().equals(schemaId)
        ).findAny();

        if (machineLearningBuildComponent.isEmpty()) {
            Model<Label> model = aiModelBuilder.loadModel(lastBuild.get().getId());
            loadingMap.add(
                    MachineLearningBuildComponent.builder()
                            .schemaId(schemaId)
                            .lastBuildId(lastBuild.get().getId())
                            .model(model)
                            .build()
            );

        } else {
            if (!StringUtils.equals(machineLearningBuildComponent.get().getLastBuildId(), lastBuild.get().getId())) {
                machineLearningBuildComponent.get().setLastBuildId(lastBuild.get().getId());
                Model<Label> model = aiModelBuilder.loadModel(lastBuild.get().getId());
                machineLearningBuildComponent.get().setModel(model);
            }

        }
    }

    @Override
    public Map<String, String> classify(Operation operation) throws Exception {

        List<MachineLearningAttributes> machineLearningAttributes = machineLearningAttributesRepository.findByOperationSchemaId(operation.getOperationSchemaId());

        if (machineLearningAttributes.isEmpty()) {
            log.warn("No attributes has been found with the provided schema Id");
            return Collections.emptyMap();
        }

        Optional<Model<Label>> model = loadingMap.stream().filter(ml -> Objects.equals(ml.getSchemaId(), operation.getOperationSchemaId()))
                .findFirst()
                .map(MachineLearningBuildComponent::getModel);

        if (model.isEmpty()) {
            log.warn("Model should be loaded before it can be used, returning 0 as a score");
            return Collections.emptyMap();
        }

        JsonNode jsonNode = objectMapper.readTree(operation.getRequestContent());

        LabelFactory labelFactory = new LabelFactory();

        var textPipeline = new BasicPipeline(new BreakIteratorTokenizer(Locale.US), 2);
        var fieldProcessors = machineLearningAttributes.stream()
                .map(attribute -> switch (attribute.getAttributeType().toLowerCase()) {
                    case "string" -> new TextFieldProcessor(attribute.getAttributeName(), textPipeline);
                    case "nominal" -> new IdentityProcessor(attribute.getAttributeName());
                    default -> new DoubleFieldProcessor(attribute.getAttributeName());
                }).toList();

        var responseProcessor = new FieldResponseProcessor<>("class", "unknown", labelFactory);

        var rowProcessor = new RowProcessor.Builder<Label>()
                .setFieldProcessors(fieldProcessors)
                .build(responseProcessor);

        Map<String, String> row = new HashMap<>();

        machineLearningAttributes
                .forEach(machineLearningAttribute -> {
                    String value = jsonNode.at(machineLearningAttribute.getAttributeValuePath()).asText();
                    log.info("Key={}, Value={}", machineLearningAttribute.getAttributeName(), value);
                    row.put(machineLearningAttribute.getAttributeName(), value);
                });

        Optional<Example<Label>> example = rowProcessor.generateExample(row, false);
        return example.map(features -> {
                    var prediction = model.get().predict(features).getOutput();
                    return Map.of("score", String.valueOf(prediction.getScore()), "result", prediction.getLabel());
                }
                ).orElse(Collections.emptyMap());

    }

    @Getter
    @Setter
    @Builder
    static class MachineLearningBuildComponent {
        private Long schemaId;
        private String lastBuildId;
        private Model<Label> model;

    }
}
