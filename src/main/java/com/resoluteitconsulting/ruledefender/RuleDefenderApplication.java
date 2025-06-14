package com.resoluteitconsulting.ruledefender;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class RuleDefenderApplication {

	public static void main(String[] args) {
		SpringApplication.run(RuleDefenderApplication.class, args);
	}

}
