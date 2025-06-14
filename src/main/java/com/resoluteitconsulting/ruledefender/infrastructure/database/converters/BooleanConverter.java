package com.resoluteitconsulting.ruledefender.infrastructure.database.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

@Converter
public class BooleanConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean b) {
        return Objects.requireNonNullElse(b, Boolean.FALSE) == Boolean.TRUE ?  "Y" : "N";
    }

    @Override
    public Boolean convertToEntityAttribute(String s) {
        return StringUtils.equalsIgnoreCase(s, "Y");
    }

}
