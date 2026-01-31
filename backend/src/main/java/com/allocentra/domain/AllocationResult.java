package com.allocentra.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents the result of an allocation for a specific request
 */
@Entity
@Table(name = "allocation_results", indexes = {
    @Index(name = "idx_run_id", columnList = "run_id"),
    @Index(name = "idx_request_id", columnList = "request_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private AllocationRun run;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Request.RequestStatus status;

    @Column(precision = 19, scale = 2)
    private BigDecimal amountRequested;

    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal amountAllocated = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal quantityRequested;

    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal quantityAllocated = BigDecimal.ZERO;

    @Column(nullable = false)
    private Double score;

    @Column(nullable = false)
    private Integer rank;

    @Column(length = 1000)
    private String reason;

    @ElementCollection
    @CollectionTable(name = "allocation_result_constraints",
        joinColumns = @JoinColumn(name = "result_id"))
    @Column(name = "constraint_type")
    @Builder.Default
    private java.util.List<String> constraintViolations = new java.util.ArrayList<>();

    @OneToOne(mappedBy = "result", cascade = CascadeType.ALL, orphanRemoval = true)
    private DecisionExplanation explanation;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
