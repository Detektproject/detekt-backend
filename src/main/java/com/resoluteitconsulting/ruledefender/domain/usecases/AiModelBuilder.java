package com.resoluteitconsulting.ruledefender.domain.usecases;

import org.tribuo.Model;
import org.tribuo.classification.Label;

import java.io.IOException;
import java.util.List;

public interface AiModelBuilder {


    List<Double> trainModel(String buildId, Long schemaId) throws IOException;

    Model<Label> loadModel(String buildId) throws IOException, ClassNotFoundException;

}
