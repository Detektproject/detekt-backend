package com.resoluteitconsulting.ruledefender.infrastructure.database.model;

import com.resoluteitconsulting.ruledefender.infrastructure.database.converters.BooleanConverter;
import com.resoluteitconsulting.ruledefender.infrastructure.database.converters.HashMapConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Convert(converter= HashMapConverter.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> features = new HashMap<>();

    @Column(nullable = false, length = 50)
    private String billingCycle;

    @Convert(converter= BooleanConverter.class)
    @Column(nullable = false, length = 1)
    private Boolean isDefault;

}