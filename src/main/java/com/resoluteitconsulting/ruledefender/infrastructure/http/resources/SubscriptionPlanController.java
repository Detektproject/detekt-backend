package com.resoluteitconsulting.ruledefender.infrastructure.http.resources;


import com.resoluteitconsulting.ruledefender.infrastructure.database.model.SubscriptionPlan;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.SubscriptionPlanRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/subscription-plan")
@Slf4j
@AllArgsConstructor
public class SubscriptionPlanController {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @GetMapping
    public ResponseEntity<List<SubscriptionPlan>> getSupportedSubscriptionPlan(){
        List<SubscriptionPlan> subscriptionPlans = subscriptionPlanRepository.findAll();
        return ResponseEntity.ok(subscriptionPlans);
    }


}
