package com.resoluteitconsulting.ruledefender.domain.model;

import java.util.List;

public record Rule (
        Long id,
        List<Criterion> criteria,
        double weight,
        TimeWindowType intervalType,
        Long intervalValue

){
}
