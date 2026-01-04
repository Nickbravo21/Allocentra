package com.allocentra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Allocentra Backend Application
 * 
 * Professional resource allocation system with:
 * - Multi-category allocation (money, personnel, vehicles, equipment, hours)
 * - Intelligent scoring and constraint-based allocation
 * - Complete explainability for every decision
 * - Scenario simulation for what-if analysis
 * - Full audit trail
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class AllocentraApplication {

    public static void main(String[] args) {
        SpringApplication.run(AllocentraApplication.class, args);
    }

}
