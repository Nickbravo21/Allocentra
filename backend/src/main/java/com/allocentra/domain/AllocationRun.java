package com.allocentra.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an allocation run (execution of the allocation engine)
 */
@Entity
@Table(name = "allocation_runs", indexes = {
    @Index(name = "idx_cycle_id", columnList = "cycle_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationRun {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_id", nullable = false)
    private AllocationCycle cycle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RunStatus status = RunStatus.PENDING;

    @Column(nullable = false)
    private String engineVersion;

    @Column(nullable = false)
    @Builder.Default
    private boolean allowPartialAllocations = true;

    @Column(columnDefinition = "TEXT")
    private String categoryCapsJson;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "run", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AllocationResult> results = new ArrayList<>();

    // Summary statistics
    private Integer totalRequests;
    private Integer approvedCount;
    private Integer partialCount;
    private Integer deferredCount;
    private Integer deniedCount;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalAllocated;

    @Column(precision = 10, scale = 4)
    private Double budgetUtilization;

    // Execution metrics
    private Long executionTimeMs;
    private Double progress;
    private String currentPhase;

    @Column(length = 2000)
    private String errorMessage;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant startedAt;
    private Instant completedAt;

    private String createdBy;

    public enum RunStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED
    }

    // Helper methods
    public void addResult(AllocationResult result) {
        results.add(result);
        result.setRun(this);
    }
}
