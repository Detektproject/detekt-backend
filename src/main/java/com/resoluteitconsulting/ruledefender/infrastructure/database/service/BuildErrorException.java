package com.resoluteitconsulting.ruledefender.infrastructure.database.service;

public class BuildErrorException extends RuntimeException {

    public BuildErrorException(String message) {
        super(message);
    }

}
