package com.allocentra.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Detailed explanation for an allocation decision
 */
@Entity
@Table(name = "decision_explanations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionExplanation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id", nullable = false)
    private AllocationResult result;

    // Score breakdown as JSON
    @Column(columnDefinition = "TEXT")
    private String scoreBreakdownJson;

    @Column(length = 1000)
    private String reasonApproved;

    @Column(length = 1000)
    private String reasonDenied;

    @Column(length = 1000)
    private String reasonPartial;

    @Column(length = 1000)
    private String reasonDeferred;

    // Comparison to next ranked request
    private String comparedToRequestId;
    private String comparedToRequestTitle;
    private Double comparedToScore;
    private Double scoreDifference;

    @Column(length = 1000)
    private String whyThisWon;

    @Column(length = 1000)
    private String whyThisLost;

    @ElementCollection
    @CollectionTable(name = "explanation_remediation",
        joinColumns = @JoinColumn(name = "explanation_id"))
    @Column(name = "suggestion")
    @Builder.Default
    private java.util.List<String> whatWouldChange = new java.util.ArrayList<>();
}
