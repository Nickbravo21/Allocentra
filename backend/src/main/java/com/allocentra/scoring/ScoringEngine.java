package com.allocentra.scoring;

import com.allocentra.domain.Request;
import lombok.Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Scoring engine that calculates request scores based on multiple factors
 * 
 * Formula: Score = (Priority × 0.30) + (Urgency × 0.25) + (Impact × 0.25) + (Risk × 0.15) + (Strategic × 0.05)
 */
@Service
public class ScoringEngine {

    @Value("${allocentra.scoring.weights.priority:0.30}")
    private double priorityWeight;

    @Value("${allocentra.scoring.weights.urgency:0.25}")
    private double urgencyWeight;

    @Value("${allocentra.scoring.weights.impact:0.25}")
    private double impactWeight;

    @Value("${allocentra.scoring.weights.risk:0.15}")
    private double riskWeight;

    @Value("${allocentra.scoring.weights.strategic:0.05}")
    private double strategicWeight;

    /**
     * Calculate total score for a request
     */
    public double calculateScore(Request request) {
        return calculateScore(request, LocalDate.now());
    }

    /**
     * Calculate score with specific evaluation date (for scenarios)
     */
    public double calculateScore(Request request, LocalDate evaluationDate) {
        ScoreBreakdown breakdown = calculateBreakdown(request, evaluationDate);
        return breakdown.getTotalScore();
    }

    /**
     * Calculate detailed score breakdown for explanations
     */
    public ScoreBreakdown calculateBreakdown(Request request) {
        return calculateBreakdown(request, LocalDate.now());
    }

    /**
     * Calculate detailed score breakdown with specific evaluation date
     */
    public ScoreBreakdown calculateBreakdown(Request request, LocalDate evaluationDate) {
        double priorityScore = calculatePriorityScore(request);
        double urgencyScore = calculateUrgencyScore(request, evaluationDate);
        double impactScore = calculateImpactScore(request);
        double riskScore = calculateRiskScore(request);
        double strategicScore = calculateStrategicScore(request);

        double totalScore = 
            (priorityScore * priorityWeight) +
            (urgencyScore * urgencyWeight) +
            (impactScore * impactWeight) +
            (riskScore * riskWeight) +
            (strategicScore * strategicWeight);

        return ScoreBreakdown.builder()
            .totalScore(totalScore)
            .priority(ScoreComponent.builder()
                .value(priorityScore)
                .weight(priorityWeight)
                .contribution(priorityScore * priorityWeight)
                .build())
            .urgency(ScoreComponent.builder()
                .value(urgencyScore)
                .weight(urgencyWeight)
                .contribution(urgencyScore * urgencyWeight)
                .daysUntilDeadline(ChronoUnit.DAYS.between(evaluationDate, request.getUrgencyDeadline()))
                .build())
            .impact(ScoreComponent.builder()
                .value(impactScore)
                .weight(impactWeight)
                .contribution(impactScore * impactWeight)
                .category(request.getImpact().name())
                .build())
            .risk(ScoreComponent.builder()
                .value(riskScore)
                .weight(riskWeight)
                .contribution(riskScore * riskWeight)
                .category(request.getRisk().name())
                .build())
            .strategic(ScoreComponent.builder()
                .value(strategicScore)
                .weight(strategicWeight)
                .contribution(strategicScore * strategicWeight)
                .build())
            .build();
    }

    private double calculatePriorityScore(Request request) {
        // Priority is 1-5, use directly
        return Math.min(5.0, Math.max(1.0, request.getPriority()));
    }

    private double calculateUrgencyScore(Request request, LocalDate evaluationDate) {
        long daysUntilDeadline = ChronoUnit.DAYS.between(evaluationDate, request.getUrgencyDeadline());
        
        // If deadline passed, maximum urgency
        if (daysUntilDeadline <= 0) {
            return 5.0;
        }
        
        // Formula: 5 - min(4, days/30)
        // 0-7 days: ~5.0
        // 30 days: 4.0
        // 60 days: 3.0
        // 90 days: 2.0
        // 120+ days: 1.0
        double normalized = Math.min(4.0, daysUntilDeadline / 30.0);
        return Math.max(1.0, 5.0 - normalized);
    }

    private double calculateImpactScore(Request request) {
        return request.getImpact().getValue();
    }

    private double calculateRiskScore(Request request) {
        return request.getRisk().getValue();
    }

    private double calculateStrategicScore(Request request) {
        // Strategic alignment score (1-5)
        return Math.min(5.0, Math.max(1.0, request.getStrategic()));
    }

    @Getter
    @Builder
    public static class ScoreBreakdown {
        private double totalScore;
        private ScoreComponent priority;
        private ScoreComponent urgency;
        private ScoreComponent impact;
        private ScoreComponent risk;
        private ScoreComponent strategic;
    }

    @Getter
    @Builder
    public static class ScoreComponent {
        private double value;
        private double weight;
        private double contribution;
        private Long daysUntilDeadline;
        private String category;

        public double getPercentOfTotal() {
            return contribution / getTotalScore() * 100;
        }

        private double getTotalScore() {
            // This would need to be injected from parent, but for simplicity:
            return contribution / weight;
        }
    }
}
