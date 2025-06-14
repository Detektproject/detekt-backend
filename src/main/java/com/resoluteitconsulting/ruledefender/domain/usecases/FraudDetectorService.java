package com.resoluteitconsulting.ruledefender.domain.usecases;

import com.resoluteitconsulting.ruledefender.domain.model.Rule;
import com.resoluteitconsulting.ruledefender.domain.model.Transaction;

import java.util.List;

public interface FraudDetectorService {

    List<Rule> evaluate(Transaction lastTransaction, List<Transaction> historicalRequests, List<Rule> rules);

}
