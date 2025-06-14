package com.resoluteitconsulting.ruledefender.infrastructure.database.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.MachineLearningAttributes;
import com.resoluteitconsulting.ruledefender.infrastructure.database.model.Operation;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.MachineLearningAttributesRepository;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.OperationRepository;
import com.resoluteitconsulting.ruledefender.domain.usecases.AiModelBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.tribuo.*;
import org.tribuo.classification.Label;
import org.tribuo.classification.LabelFactory;
import org.tribuo.classification.dtree.CARTClassificationTrainer;
import org.tribuo.classification.ensemble.VotingCombiner;
import org.tribuo.classification.evaluation.LabelEvaluator;
import org.tribuo.common.tree.RandomForestTrainer;
import org.tribuo.data.columnar.RowProcessor;
import org.tribuo.data.columnar.processors.field.DoubleFieldProcessor;
import org.tribuo.data.columnar.processors.field.IdentityProcessor;
import org.tribuo.data.columnar.processors.field.TextFieldProcessor;
import org.tribuo.data.columnar.processors.response.FieldResponseProcessor;
import org.tribuo.data.csv.CSVDataSource;
import org.tribuo.data.text.impl.BasicPipeline;
import org.tribuo.evaluation.TrainTestSplitter;
import org.tribuo.util.tokens.impl.BreakIteratorTokenizer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiModelBuilderImpl implements AiModelBuilder {

    @Value("${app.ai.model.folder}")
    private String modelFolder;

    @Value("${app.ai.extraction.folder}")
    private String extractionFolder;

    private final ObjectMapper objectMapper;
    private final OperationRepository operationRepository;
    private final MachineLearningAttributesRepository machineLearningAttributesRepository;

    @Override
    public List<Double> trainModel(String buildId, Long schemaId) throws IOException {

        Path generatedCsvFile = Path.of(StringUtils.join(extractionFolder, buildId, ".csv"));

        if (!generatedCsvFile.getParent().toFile().exists())
            Files.createDirectories(Path.of(extractionFolder));

        List<MachineLearningAttributes> machineLearningAttributes = machineLearningAttributesRepository.findByOperationSchemaId(schemaId);

        if (machineLearningAttributes.isEmpty())
            throw new BuildErrorException("No attribute has been configured for the provided schema id");

        Slice<Operation> slice = operationRepository.findAllByOperationSchemaId(schemaId, PageRequest.of(0, 1000));
        List<Operation> operations = slice.getContent();

        if (operations.isEmpty())
            throw new BuildErrorException("No operation has been found to train the model");


        String header = machineLearningAttributes.stream()
                .map(MachineLearningAttributes::getAttributeName)
                .collect(Collectors.joining("|"));

        Files.writeString(generatedCsvFile, StringUtils.join(header, "|", "class", "\n"), StandardOpenOption.APPEND, StandardOpenOption.CREATE);

        extractOperations(generatedCsvFile, operations, machineLearningAttributes);

        while (slice.hasNext()) {
            slice = operationRepository.findAllByOperationSchemaId(schemaId, slice.nextPageable());
            operations = slice.getContent();
            extractOperations(generatedCsvFile, operations, machineLearningAttributes);
        }


        LabelFactory labelFactory = new LabelFactory();

        var textPipeline = new BasicPipeline(new BreakIteratorTokenizer(Locale.US), 2);
        var fieldProcessors = machineLearningAttributes.stream()
                .map(attribute -> switch (attribute.getAttributeType().toLowerCase()) {
                    case "string" -> new TextFieldProcessor(attribute.getAttributeName(), textPipeline);
                    case "nominal" -> new IdentityProcessor(attribute.getAttributeName());
                    default -> new DoubleFieldProcessor(attribute.getAttributeName());
                }).toList();

        var responseProcessor = new FieldResponseProcessor<>("class", "0", labelFactory);

        var rowProcessor = new RowProcessor.Builder<Label>()
                .setFieldProcessors(fieldProcessors)
                .build(responseProcessor);


        var dataSource = new CSVDataSource<>(generatedCsvFile, rowProcessor, true, '|');

        var splitter = new TrainTestSplitter<>(dataSource, 0.7f, 0L);

        Dataset<Label> trainData = new MutableDataset<>(splitter.getTrain());

        log.info("Number of train examples = " + trainData.size());
        log.info("Number of train features = " + trainData.getFeatureMap().size());
        log.info("Label domain = " + trainData.getOutputIDInfo().getDomain());

        Dataset<Label> evalData = new MutableDataset<>(splitter.getTest());

        log.info("Output = " + trainData.getExample(0).getOutput().toString());
        log.info("Metadata = " + trainData.getExample(0).getMetadata());
        log.info("Weight = " + trainData.getExample(0).getWeight());
        log.info("Features = [" + StreamSupport.stream(trainData.getExample(0).spliterator(), false).map(Feature::toString).collect(Collectors.joining(",")) + "]");


        var cartTrainer = new CARTClassificationTrainer(Integer.MAX_VALUE, 0.7f, Trainer.DEFAULT_SEED);

        var randomForest = new RandomForestTrainer<>(cartTrainer, new VotingCombiner(), 10);

        Model<Label> model = randomForest.train(trainData);

        var evaluator = new LabelEvaluator();

        var evaluationResult = evaluator.evaluate(model, evalData);

        log.info("Validation Accuracy: " + evaluationResult.accuracy());
        log.info("Validation Accuracy: " + evaluationResult.getConfusionMatrix().toString());

        if (!Path.of(modelFolder).toFile().exists())
            Files.createDirectories(Path.of(modelFolder));

        log.info("Writing model to filesystem");
        File modelFile = new File(StringUtils.join(modelFolder, buildId, ".ser"));
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(modelFile))) {
            oos.writeObject(model);
        }
        log.info("End Writing model to filesystem");

        return List.of(evaluationResult.accuracy(), (double) trainData.size());
    }

    @Override
    public Model<Label> loadModel(String buildId) throws IOException, ClassNotFoundException {
        File modelFile = new File(StringUtils.join(modelFolder, buildId, ".ser"));
        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(modelFile)))) {
            Model<Label> model = (Model<Label>) ois.readObject();
            return model;
        }
    }


    private void extractOperations(Path generatedCsvFile, List<Operation> operations, List<MachineLearningAttributes> machineLearningAttributes) throws IOException {
        Files.write(
                generatedCsvFile,
                operations.stream()
                        .map(item -> {
                            try {
                                String requestContent = item.getRequestContent();

                                JsonNode jsonNode = objectMapper.readTree(requestContent);

                                if (StringUtils.isEmpty(requestContent))
                                    return null;

                                String content = machineLearningAttributes.stream()
                                        .map(
                                                attr -> {
                                                    log.debug("Getting {} using path {}", attr.getAttributeName(), attr.getAttributeValuePath());
                                                    String value = jsonNode.at(attr.getAttributeValuePath()).asText();

                                                    return StringUtils.defaultIfBlank(value, "");
                                                }
                                        ).collect(Collectors.joining("|"));

                                return StringUtils.join(content, "|", Boolean.TRUE.equals(item.getIsAnomaly()) ? "1" : "0");

                            } catch (Exception ex) {
                                log.error("An error occurred while parsing request message: ", ex);
                                return null;
                            }

                        })
                        .filter(Objects::nonNull)
                        .toList()
                ,
                StandardOpenOption.APPEND);
    }


}
