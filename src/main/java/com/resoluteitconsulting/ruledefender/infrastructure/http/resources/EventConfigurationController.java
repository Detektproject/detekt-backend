package com.resoluteitconsulting.ruledefender.infrastructure.http.resources;

import com.resoluteitconsulting.ruledefender.infrastructure.database.model.EventConfiguration;
import com.resoluteitconsulting.ruledefender.infrastructure.database.repository.EventConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RequestMapping("/event-configuration")
@RestController
public class EventConfigurationController {


    @Autowired
    private EventConfigurationRepository eventConfigurationRepository;

    @GetMapping
    public ResponseEntity<List<EventConfiguration>> getEventConfigurations() {

        List<EventConfiguration> eventConfigurations = eventConfigurationRepository.findAll();

        return ResponseEntity.ok(eventConfigurations);

    }

    @PostMapping
    public ResponseEntity<EventConfiguration> addEventConfiguration(Authentication authentication, @RequestBody EventConfiguration eventConfiguration) {

        eventConfigurationRepository.save(eventConfiguration);

        return ResponseEntity.ok(eventConfiguration);
    }


}
