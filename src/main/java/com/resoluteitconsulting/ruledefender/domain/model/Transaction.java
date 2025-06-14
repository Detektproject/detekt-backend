package com.resoluteitconsulting.ruledefender.domain.model;


import com.jayway.jsonpath.DocumentContext;

import java.time.Instant;

public record Transaction (
        DocumentContext trxJsonContext,
        Instant trxInstant
){

}
