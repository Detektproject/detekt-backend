package com.resoluteitconsulting.ruledefender.infrastructure.database.service;

import com.jayway.jsonpath.DocumentContext;
import com.resoluteitconsulting.ruledefender.domain.model.*;
import com.resoluteitconsulting.ruledefender.domain.usecases.FraudDetectorService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class FraudDetectorServiceImpl implements FraudDetectorService {

    @Override
    public List<Rule> evaluate(Transaction lastTransaction, List<Transaction> historicalRequests, List<Rule> rules) {
        List<Rule> detectedRules = new ArrayList<>();
        for (Rule rule : rules) {
            boolean allCriteriaMatch = true;

            for (Criterion criterion : rule.criteria()) {
                boolean criteriaMatch;
                if (criterion.aggregationType() == AggregationType.NONE) {
                    criteriaMatch = evaluateCriterion(lastTransaction.trxJsonContext(), criterion);
                } else {
                    historicalRequests.add(lastTransaction);
                    criteriaMatch = evaluateAggregatedCriteria(rule, lastTransaction.trxInstant(), criterion, historicalRequests);
                }

                if (!criteriaMatch) {
                    allCriteriaMatch = false;
                    break; // If any criterion fails, skip the rest and do not add the rule.
                }
            }

            if (allCriteriaMatch) {
                detectedRules.add(rule);
            }
        }
        return detectedRules;
    }

    private boolean evaluateCriterion(DocumentContext jsonRequest, Criterion criterion) {
        // All conditions must be met (logical AND)
        for (Condition condition : criterion.conditions()) {
            String extractedValue = jsonRequest.read(condition.fieldPath(), String.class);
            String value = StringUtils.startsWith(condition.value(), "$") ? jsonRequest.read(condition.value(), String.class) : condition.value();
            if (!evaluateCondition(extractedValue, condition.operator(), value)) {
                return false;
            }
        }
        return true;
    }

    private boolean evaluateCondition(Object extractedValue, Operator operator, Object expectedValue) {
        return switch (operator) {
            case EQUAL -> extractedValue.equals(expectedValue);
            case NOT_EQUAL -> !extractedValue.equals(expectedValue);
            case GREATER_THAN ->
                    Double.parseDouble(extractedValue.toString()) > Double.parseDouble(expectedValue.toString());
            case LESS_THAN ->
                    Double.parseDouble(extractedValue.toString()) < Double.parseDouble(expectedValue.toString());
            case GREATER_THAN_OR_EQUAL ->
                    Double.parseDouble(extractedValue.toString()) >= Double.parseDouble(expectedValue.toString());
            case LESS_THAN_OR_EQUAL ->
                    Double.parseDouble(extractedValue.toString()) <= Double.parseDouble(expectedValue.toString());
            default -> false;
        };
    }

    private boolean evaluateAggregatedCriteria(Rule rule, Instant baseline, Criterion criterion, List<Transaction> historicalTransactions) {
        List<Transaction> filteredTransactions = historicalTransactions.stream()
                .filter(tx ->
                        evaluateTimeWindow(tx.trxInstant(), baseline, rule.intervalType(), rule.intervalValue()) &&
                                evaluateAllConditions(tx.trxJsonContext(), criterion.conditions()))
                .toList();

        return switch (criterion.aggregationType()) {
            case SUM -> {
                double sumValue = filteredTransactions.stream()
                        .mapToDouble(tx -> tx.trxJsonContext().read(criterion.aggregationPath()))
                        .sum();
                yield evaluateCondition(sumValue, Operator.GREATER_THAN_OR_EQUAL, criterion.aggregationValue());
            }
            case COUNT -> {
                long countValue = filteredTransactions.size();
                yield evaluateCondition(countValue, Operator.GREATER_THAN_OR_EQUAL, criterion.aggregationValue());
            }
            case AVG -> {
                double avgValue = filteredTransactions.stream()
                        .mapToDouble(tx -> tx.trxJsonContext().read(criterion.aggregationPath()))
                        .average()
                        .orElse(0);
                yield evaluateCondition(avgValue, Operator.EQUAL, criterion.aggregationValue());
            }
            case MAX -> {
                double maxValue = filteredTransactions.stream()
                        .mapToDouble(tx -> tx.trxJsonContext().read(criterion.aggregationPath()))
                        .max()
                        .orElse(0);
                yield evaluateCondition(maxValue, Operator.EQUAL, criterion.aggregationValue());
            }
            case MIN -> {
                double minValue = filteredTransactions.stream()
                        .mapToDouble(tx -> tx.trxJsonContext().read(criterion.aggregationPath()))
                        .min()
                        .orElse(0);
                yield evaluateCondition(minValue, Operator.EQUAL, criterion.aggregationValue());
            }
            default -> true;
        };
    }

    private boolean evaluateAllConditions(DocumentContext tx, List<Condition> conditions) {
        // All conditions must be true
        for (Condition condition : conditions) {
            Object extractedValue = tx.read(condition.fieldPath());
            Object value = StringUtils.startsWith(condition.value(), "$") ? tx.read(condition.value()) : condition.value();
            if (!evaluateCondition(extractedValue, condition.operator(), value)) {
                return false;
            }
        }
        return true;
    }

    private boolean evaluateTimeWindow(Instant trxInstant, Instant trxBaseline, TimeWindowType timeWindowType, long timeWindowValue) {

        return switch (timeWindowType) {
            case MINUTE -> trxBaseline.minus(timeWindowValue, ChronoUnit.MINUTES).isBefore(trxInstant);
            case HOUR -> trxBaseline.minus(timeWindowValue, ChronoUnit.HOURS).isBefore(trxInstant);
            case DAY -> trxBaseline.minus(timeWindowValue, ChronoUnit.DAYS).isBefore(trxInstant);
            case NONE -> true;
        };

    }


}
